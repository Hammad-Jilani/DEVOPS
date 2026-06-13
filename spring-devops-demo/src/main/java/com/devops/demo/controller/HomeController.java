package com.devops.demo.controller;

import com.devops.demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class HomeController {

    @Autowired
    private TaskService taskService;

    @Value("${app.version:2.0.0}")
    private String appVersion;

    @Value("${app.environment:development}")
    private String environment;

    // ── Home — supports search + filter query params ──────────────────────────

    @GetMapping("/")
    public String home(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String priority,
            @RequestParam(required = false, defaultValue = "") String status,
            Model model) {

        long total     = taskService.getTotalCount();
        long completed = taskService.getCompletedCount();
        long pending   = total - completed;
        int  pct       = (total > 0) ? (int) (completed * 100 / total) : 0;

        model.addAttribute("tasks",          taskService.searchTasks(search, priority, status));
        model.addAttribute("completedCount", completed);
        model.addAttribute("totalCount",     total);
        model.addAttribute("pendingCount",   pending);
        model.addAttribute("progressPct",    pct);
        model.addAttribute("appVersion",     appVersion);
        model.addAttribute("environment",    environment);

        // Preserve filter state in the form after redirect
        model.addAttribute("currentSearch",   search);
        model.addAttribute("currentPriority", priority);
        model.addAttribute("currentStatus",   status);

        return "index";
    }

    // ── Add task ──────────────────────────────────────────────────────────────

    @PostMapping("/tasks/add")
    public String addTask(
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "") String description,
            @RequestParam String priority,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            RedirectAttributes ra) {

        taskService.addTask(title, description, priority, dueDate);
        // Flash attribute → picked up by JS toast system after redirect
        ra.addFlashAttribute("toast", "Task \"" + title + "\" added successfully.");
        ra.addFlashAttribute("toastType", "success");
        return "redirect:/";
    }

    // ── Toggle completed ──────────────────────────────────────────────────────

    @PostMapping("/tasks/{id}/toggle")
    public String toggleTask(@PathVariable Long id, RedirectAttributes ra) {
        boolean ok = taskService.toggleTask(id);
        if (ok) {
            ra.addFlashAttribute("toast", "Task status updated.");
            ra.addFlashAttribute("toastType", "success");
        } else {
            ra.addFlashAttribute("toast", "Task not found.");
            ra.addFlashAttribute("toastType", "error");
        }
        return "redirect:/";
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes ra) {
        boolean ok = taskService.deleteTask(id);
        if (ok) {
            ra.addFlashAttribute("toast", "Task deleted.");
            ra.addFlashAttribute("toastType", "error"); // red toast fits "destructive"
        } else {
            ra.addFlashAttribute("toast", "Task not found.");
            ra.addFlashAttribute("toastType", "error");
        }
        return "redirect:/";
    }
}