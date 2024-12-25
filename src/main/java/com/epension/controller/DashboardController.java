package com.epension.controller;

import com.epension.model.User;
import com.epension.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import com.epension.service.JwtService;

@Controller
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081" }, allowCredentials = "true")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/dashboard")
    public String redirectToDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByPhoneNumber(auth.getName());

        if (user != null) {
            if ("ROLE_ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/pensioner/dashboard";
            }
        }

        return "redirect:/login";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String adminDashboard(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByPhoneNumber(auth.getName());

            logger.debug("Admin dashboard access - User: {}", user.getPhoneNumber());

            model.addAttribute("user", user);
            model.addAttribute("role", user.getRole());
            model.addAttribute("totalPensioners", userService.countPensioners());

            return "dashboard";
        } catch (Exception e) {
            logger.error("Error accessing admin dashboard", e);
            return "redirect:/login?error=access_denied";
        }
    }

    @GetMapping("/pensioner/dashboard")
    @PreAuthorize("hasRole('ROLE_PENSIONER')")
    public String pensionerDashboard(Model model, @RequestParam(required = false) String token) {
        try {
            if (token == null) {
                logger.warn("No token provided in request");
                return "redirect:/login?error=no_token";
            }

            logger.debug("Received token: {}", token);

            // Validate token and get user details
            if (!jwtService.validateToken(token)) {
                logger.warn("Invalid token provided");
                return "redirect:/login?error=invalid_token";
            }

            String phoneNumber = jwtService.getUsernameFromToken(token);
            logger.debug("Phone number from token: {}", phoneNumber);

            User user = userService.findByPhoneNumber(phoneNumber);
            logger.debug("Found user: {}", user != null ? user.getPhoneNumber() : "null");

            if (user == null) {
                logger.warn("No user found for token");
                return "redirect:/login?error=invalid_user";
            }

            // Set authentication in security context
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user.getPhoneNumber(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(user.getRole())));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            model.addAttribute("user", user);
            model.addAttribute("role", user.getRole());
            logger.info("Successfully loaded dashboard for user: {}", user.getPhoneNumber());

            return "dashboard/pensioner_dashboard";
        } catch (Exception e) {
            logger.error("Error accessing pensioner dashboard", e);
            return "redirect:/login?error=access_denied&message=" + e.getMessage();
        }
    }
}
