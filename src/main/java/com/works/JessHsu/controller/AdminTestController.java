package com.works.JessHsu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminTestController {

    @GetMapping("/api/admin/hello")
    public String adminHello() {
        return "Hello, Admin! Only ADMIN can see this.";
    }

    @GetMapping("/api/public/ping")
    public String publicPing() {
        return "pong";
    }
}