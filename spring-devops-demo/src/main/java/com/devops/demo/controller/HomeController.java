package com.devops.demo.controller;

import com.devops.demo.service.TaskService;
import com.devops.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class HomeController {

    @Autowired private TaskService taskService;
    @Autowired private UserService userService;

    @Value("${app.version:2.0.0}") private String appVersion;
    @Value("${app.environment:development}") private String environment;

    // ── Home ─────────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String home(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String priority,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "") String assigneeUsername,
            Authentication auth,
            Model model) {

        boolean isAdmin = auth.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // Admins see ALL tasks (filtered by assigneeUsername if chosen).
        // Regular users see ONLY their own assigned tasks.
        String viewerUsername = isAdmin ? null : auth.getName();

        long total     = taskService.getTotalCount();
        long completed = taskService.getCompletedCount();
        long pending   = total - completed;
        int  pct       = (total > 0) ? (int) (completed * 100 / total) : 0;

        model.addAttribute("tasks",
                taskService.searchTasks(search, priority, status, assigneeUsername, viewerUsername));
        model.addAttribute("completedCount",       completed);
        model.addAttribute("totalCount",           total);
        model.addAttribute("pendingCount",         pending);
        model.addAttribute("progressPct",          pct);
        model.addAttribute("appVersion",           appVersion);
        model.addAttribute("environment",          environment);
        model.addAttribute("isAdmin",              isAdmin);
        model.addAttribute("allUsers",             taskService.getAllUsers());

        // Preserve filter state
        model.addAttribute("currentSearch",          search);
        model.addAttribute("currentPriority",        priority);
        model.addAttribute("currentStatus",          status);
        model.addAttribute("currentAssigneeUsername", assigneeUsername);

        return "index";
    }

    // ── Add task (admin only) ─────────────────────────────────────────────────

    @PostMapping("/tasks/add")
    public String addTask(
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "") String description,
            @RequestParam String priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) Long assigneeId,
            RedirectAttributes ra) {

        taskService.addTask(title, description, priority, dueDate, assigneeId);
        ra.addFlashAttribute("toast", "Task \"" + title + "\" added successfully.");
        ra.addFlashAttribute("toastType", "success");
        return "redirect:/";
    }

    // ── Assign task (admin only) ──────────────────────────────────────────────

    @PostMapping("/tasks/{id}/assign")
    public String assignTask(
            @PathVariable Long id,
            @RequestParam(required = false) Long assigneeId,
            RedirectAttributes ra) {

        boolean ok = taskService.assignTask(id, assigneeId);
        ra.addFlashAttribute("toast", ok ? "Assignee updated." : "Task not found.");
        ra.addFlashAttribute("toastType", ok ? "success" : "error");
        return "redirect:/";
    }

    // ── Toggle completed ──────────────────────────────────────────────────────

    @PostMapping("/tasks/{id}/toggle")
    public String toggleTask(@PathVariable Long id, RedirectAttributes ra) {
        boolean ok = taskService.toggleTask(id);
        ra.addFlashAttribute("toast", ok ? "Task status updated." : "Task not found.");
        ra.addFlashAttribute("toastType", ok ? "success" : "error");
        return "redirect:/";
    }

    // ── Delete (admin only) ───────────────────────────────────────────────────

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes ra) {
        boolean ok = taskService.deleteTask(id);
        ra.addFlashAttribute("toast", ok ? "Task deleted." : "Task not found.");
        ra.addFlashAttribute("toastType", ok ? "error" : "error");
        return "redirect:/";
    }
}