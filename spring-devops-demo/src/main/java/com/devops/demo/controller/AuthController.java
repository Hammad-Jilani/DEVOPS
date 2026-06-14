package com.devops.demo.controller;

import com.devops.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // ── Login ─────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String success,
            Model model) {

        if (error   != null) model.addAttribute("error",   "Invalid username or password.");
        if (logout  != null) model.addAttribute("logout",  "You have been logged out.");
        // flash "success" arrives as a model attribute from RedirectAttributes,
        // but handle the query-param form too just in case
        return "login";
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            // email is optional — blank string when the user leaves the field empty
            @RequestParam(required = false, defaultValue = "") String email,
            RedirectAttributes ra) {

        if (username == null || username.trim().length() < 3) {
            ra.addFlashAttribute("error", "Username must be at least 3 characters.");
            return "redirect:/register";
        }
        if (password == null || password.length() < 6) {
            ra.addFlashAttribute("error", "Password must be at least 6 characters.");
            return "redirect:/register";
        }
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/register";
        }

        try {
            userService.register(username.trim(), password, email.trim());
            ra.addFlashAttribute("success",
                    "Account created! You can now log in" +
                            (email.isBlank() ? "." : " — reminder emails will go to " + email + "."));
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}