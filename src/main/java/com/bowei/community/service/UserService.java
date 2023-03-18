package com.bowei.community.service;

import com.bowei.community.dao.LoginTicketMapper;
import com.bowei.community.dao.UserMapper;
import com.bowei.community.entity.LoginTicket;
import com.bowei.community.entity.User;
import com.bowei.community.util.CommunityConstant;
import com.bowei.community.util.CommunityUtil;
import com.bowei.community.util.MailClient;
import com.bowei.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired private UserMapper userMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired private TemplateEngine templateEngine;

    @Autowired private MailClient mailClient;
    //@Autowired private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    public User findUserById(int id) {

        // return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    /** 注册 */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>(16);
        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("Variable can't be empty");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "Account can't be empty");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "Password can't be empty");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "Email can't be empty");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "This account already exist!");
            return map;
        }
        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "This email already exist!");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(
                String.format(
                        "http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url =
                domain
                        + contextPath
                        + "/activation/"
                        + user.getId()
                        + "/"
                        + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "active account email", content);


        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        // Empty value
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "Account can't be empty!");
            return map;
        }
        if (StringUtils.isBlank(username)) {
            map.put("passwordMsg", "Password can't be empty!");
            return map;
        }

        // Verify account
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "Account doesn't exist!");
            return map;
        }
        // Verify status
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "Account didn't active!");
            return map;
        }

        // Verify password
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "Password is not correct!");
            return map;
        }

        // Create login ticket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 10000));
        //loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        //loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        //return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId, String headerUrl) {
        //return userMapper.updateHeader(userId, headerUrl);
       int rows =  userMapper.updateHeader(userId, headerUrl);
       clearCache(userId);
       return rows;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    // 1. Try to get value from cache
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    // 2. Initialize cache if it can't get value from cache
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3. Clear cache when values changed
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

    //忘记密码之后给邮箱发送验证码
    public Map<String, Object> getCode(String email) {
        Map<String,Object> map = new HashMap<>();
        //空值判断
        if (StringUtils.isBlank(email)){
            map.put("emailMsg","Please enter the email！");
            return map;
        }
        //邮箱是否正确
        User user = userMapper.selectByEmail(email);
        if (user == null){
            map.put("emailMsg","This email address has not been registered yet, please register before using it!");
            return map;
        }
        //该用户还未激活
        if (user.getStatus() == 0){
            map.put("emailMsg","This mailbox is not yet activated, please go to the mailbox and activate it before using it!");
            return map;
        }
        //邮箱正确的情况下，发送验证码到邮箱
        Context context = new Context();
        context.setVariable("email",email);
        String code = CommunityUtil.generateUUID().substring(0,5);
        context.setVariable("code",code);
        String content = templateEngine.process("mail/forget", context);
        mailClient.sendMail(email, "Community verification code", content);

        map.put("code",code);//map中存放一份，为了之后和用户输入的验证码进行对比
        map.put("expirationTime", LocalDateTime.now().plusMinutes(5L));//过期时间
        return map;
    }

    //忘记密码
    public Map<String, Object> forget(String email, String verifycode, String password, HttpSession session){
        Map<String,Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(email)){
            map.put("emailMsg","Please enter your email address！");
            return map;
        }
        if (StringUtils.isBlank(verifycode)){
            map.put("codeMsg","Please enter the verification code！");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","Please enter a new password！");
            return map;
        }

        //邮箱在获取验证码那一步已经验证过了，是有效的邮箱，且验证码也有效
        User user = userMapper.selectByEmail(email);
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(),password);

        return map;
    }


}
