package com.springboot.MyTodoList.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

    // Handle all paths that don't have a dot (file extension)
    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
        // Forward to home page so that route is preserved for SPA routing
        return "forward:/index.html";
    }

    // Special handler for deep-linked routes
    @GetMapping(value = {"/login/**", "/signup/**", "/home/**", "/board/**", "/sprint/**", "/dashboard/**", "/team/**", "/test-auth/**"})
    public String handleSpaRoutes() {
        // Forward to home page
        return "forward:/index.html";
    }
}
