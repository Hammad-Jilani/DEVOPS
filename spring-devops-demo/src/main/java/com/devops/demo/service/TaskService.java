package com.devops.demo.service;

import com.devops.demo.model.Task;
import com.devops.demo.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Seed four representative tasks on first startup so the UI isn't empty.
     * The check prevents re-seeding if the app restarts against a persistent DB.
     */
    @PostConstruct
    public void seedData() {
        if (taskRepository.count() > 0) return;

        Task t1 = new Task(null, "Setup EC2 Instance",
                "Launch and configure AWS EC2", true, "HIGH");
        t1.setDueDate(LocalDate.now().minusDays(3)); // already done — no overdue badge

        Task t2 = new Task(null, "Configure GitHub Actions",
                "Setup CI/CD pipeline YAML", true, "HIGH");
        t2.setDueDate(LocalDate.now().minusDays(1));

        Task t3 = new Task(null, "Deploy Spring App",
                "Deploy JAR to EC2 via SSH", false, "MEDIUM");
        t3.setDueDate(LocalDate.now()); // due today

        Task t4 = new Task(null, "Setup Health Checks",
                "Configure /actuator/health endpoint", false, "LOW");
        t4.setDueDate(LocalDate.now().plusDays(7)); // upcoming

        taskRepository.saveAll(List.of(t1, t2, t3, t4));
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Filtered / searched task list.
     * Any param may be null or blank — the repo query handles it gracefully.
     */
    public List<Task> searchTasks(String search, String priority, String status) {
        return taskRepository.search(search, priority, status);
    }

    public long getCompletedCount() {
        return taskRepository.countByCompleted(true);
    }

    public long getTotalCount() {
        return taskRepository.count();
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    @Transactional
    public Task addTask(String title, String description, String priority, LocalDate dueDate) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setCompleted(false);
        task.setDueDate(dueDate);
        return taskRepository.save(task);
    }

    @Transactional
    public boolean toggleTask(Long id) {
        Optional<Task> opt = taskRepository.findById(id);
        if (opt.isEmpty()) return false;
        Task task = opt.get();
        task.setCompleted(!task.isCompleted());
        taskRepository.save(task);
        return true;
    }

    @Transactional
    public boolean deleteTask(Long id) {
        if (!taskRepository.existsById(id)) return false;
        taskRepository.deleteById(id);
        return true;
    }
}