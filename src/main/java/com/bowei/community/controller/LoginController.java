package com.bowei.community.controller;

import com.bowei.community.entity.User;
import com.bowei.community.service.UserService;
import com.bowei.community.util.CommunityConstant;
import com.google.code.kaptcha.Producer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;
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
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // Create kaptcha code
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // Store kaptcha code into session
        session.setAttribute("kaptcha", text);

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
                        HttpSession session, HttpServletResponse response) {
        // Check verification code
        String kaptcha = (String) session.getAttribute("kaptcha");
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
}
