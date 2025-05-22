package com.mohamedoujdid.annotationplatform.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "admin/dashboard"; // Corresponds to templates/admin/dashboard.html
    }
}
