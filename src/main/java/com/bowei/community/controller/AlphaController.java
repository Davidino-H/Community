package com.bowei.community.controller;

import com.bowei.community.service.AlphaService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // Get request data
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        System.out.println(request.getParameter("code"));

        //Return response data
        response.setContentType("text/html;charset=utf-8");
        try(PrintWriter printWriter = response.getWriter();) {
            printWriter.write("<h1>Chatime<h1>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // Get Request
    //   /students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    //   /student/123
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "a student";
    }


    // POST request
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    // response html data
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("name", "张三");
        mav.addObject("age", "27");
        mav.setViewName("/demo/view");
        return mav;
    }

    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model) {
        model.addAttribute("name", "University of Melbourne");
        model.addAttribute("age", "100");
        return "/demo/view";
    }

    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getMap(){
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "Bowei");
        emp.put("age", "20");
        emp.put("salary", "1000");
        return emp;
    }

    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getMaps(){
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "Bowei");
        emp.put("age", "20");
        emp.put("salary", "1000");
        list.add(emp);

        emp.put("name", "Yunqing");
        emp.put("age", "18");
        emp.put("salary", "1100");
        list.add(emp);
        return list;
    }
}
