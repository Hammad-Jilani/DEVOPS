package com.devops.demo.controller;

import com.devops.demo.service.EmailReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;

/**
 * Admin-only endpoints.
 * @PreAuthorize blocks non-admins at the method level (defence in depth on top
 * of the SecurityConfig route rules).
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private EmailReminderService emailReminderService;

    /**
     * POST /admin/reminders/trigger
     * Manually fires the reminder batch right now.
     * Useful for testing email config without waiting for the daily cron.
     */
    @PostMapping("/reminders/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public String triggerReminders(RedirectAttributes ra) {
        int sent = emailReminderService.triggerNow();
        ra.addFlashAttribute("toast",
                sent == 0
                        ? "No tasks due tomorrow with assigned users — no emails sent."
                        : sent + " reminder email(s) sent successfully.");
        ra.addFlashAttribute("toastType", sent == 0 ? "error" : "success");
        return "redirect:/";
    }
}