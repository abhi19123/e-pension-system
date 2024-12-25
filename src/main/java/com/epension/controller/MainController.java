package com.epension.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "home";  // Changed to return home page
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/pensioner/dashboard")
    public String pensionerDashboard() {
        return "dashboard";  // Using the same dashboard template with role-based content
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "dashboard";  // Using the same dashboard template with role-based content
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/payment-history")
    public String paymentHistory() {
        return "payment_history";
    }

    @GetMapping("/raise-issue")
    public String raiseIssue() {
        return "raise_issue";
    }

    @GetMapping("/track-application")
    public String trackApplication() {
        return "track_application";
    }

    @GetMapping("/apply-pension")
    public String applyPension() {
        return "pension_application";
    }

    @GetMapping("/forgot_password")
    public String forgotPassword() {
        return "forgot_password";
    }

    @GetMapping("/update_profile")
    public String updateProfile() {
        return "update_profile";
    }

    @GetMapping("/pension_application")
    public String pensionApplication() {
        return "pension_application";
    }

    @GetMapping("/pension_management")
    public String pensionManagement() {
        return "pension_management";
    }

    @GetMapping("/chatbot")
    public String chatbot() {
        return "chatbot";
    }
}
