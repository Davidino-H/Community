package com.bowei.community.controller;

import com.bowei.community.entity.User;
import com.bowei.community.service.UserService;
import com.bowei.community.util.CommunityConstant;
import com.bowei.community.util.CommunityUtil;
import com.bowei.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage() {
        return "/site/forget";
    }



    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "Register successfully, please check your activation email.");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }
        else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }
    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "Active successfully.");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "Activate failure, this account already been used.");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "Activate failure, the activation code you provided is incorrect.");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response) {
        // Create kaptcha code
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // Store kaptcha code into session
        //session.setAttribute("kaptcha", text);

        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // Store kaptcha code into redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);
        // Show image on the web page
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe, Model model,
                         HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // Check verification code
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "Verification code is not correct!");
            return "/site/login";
        }
        // Check account and password
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECODES : DEFAULT_EXPIRED_SECODES;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }

    //忘记密码之后获取验证码
    @RequestMapping(path = "/getCode", method = RequestMethod.GET)
    public String getCode(String email, Model model,HttpSession session) {
        Map<String, Object> map = userService.getCode(email);
        if (map.containsKey("emailMsg")) {//有错误的情况下
            model.addAttribute("emailMsg", map.get("emailMsg"));
        } else {//正确的情况下，向邮箱发送了验证码
            model.addAttribute("msg", "验证码已经发送到您的邮箱，5分钟内有效！");
            //将验证码存放在 session 中，后序和用户输入的信息进行比较
            session.setAttribute("code",map.get("code"));
            //后序判断用户输入验证码的时候验证码是否已经过期
            session.setAttribute("expirationTime",map.get("expirationTime"));
        }
        return "site/forget";
    }

    //忘记密码
    @RequestMapping(path = "/forget", method = RequestMethod.POST)
    public String forget(Model model, String email, String verifycode, String password, HttpSession session) {
        //验证验证码是否正确
        if (!verifycode.equals(session.getAttribute("code"))) {
            model.addAttribute("codeMsg", "输入的验证码不正确！");
            return "site/forget";
        }
        //验证码是否过期
        if (LocalDateTime.now().isAfter((LocalDateTime) session.getAttribute("expirationTime"))) {
            model.addAttribute("codeMsg", "输入的验证码已过期，请重新获取验证码！");
            return "site/forget";
        }
        Map<String, Object> map = userService.forget(email, verifycode, password, session);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "密码修改成功，可以使用新密码登录了!");
            model.addAttribute("target", "/login");
            return "site/operate-result";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("codeMsg", map.get("codeMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }

}
