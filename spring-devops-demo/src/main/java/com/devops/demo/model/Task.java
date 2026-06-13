package com.devops.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private boolean completed;

    @Column(nullable = false)
    private String priority;

    /**
     * Optional due date. Null means no deadline set.
     * Used for overdue highlighting in the UI.
     */
    private LocalDate dueDate;

    // ── Convenience constructor (no dueDate) for seed data ──────────────────
    public Task(Long id, String title, String description, boolean completed, String priority) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.completed   = completed;
        this.priority    = priority;
        this.dueDate     = null;
    }

    // ── Derived helper — used in Thymeleaf to drive overdue badge ───────────
    @Transient
    public boolean isOverdue() {
        return !completed && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    @Transient
    public boolean isDueToday() {
        return !completed && dueDate != null && dueDate.isEqual(LocalDate.now());
    }
}