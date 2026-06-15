package com.devops.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
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

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    // ── Convenience constructor without dueDate/assignee (for seed data) ─────
    public Task(Long id, String title, String description,
                boolean completed, String priority) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.completed   = completed;
        this.priority    = priority;
    }

    @Transient
    public boolean isOverdue() {
        return !completed && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    @Transient
    public boolean isDueToday() {
        return !completed && dueDate != null && dueDate.isEqual(LocalDate.now());
    }

    @Transient
    public boolean isDueTomorrow() {
        return !completed && dueDate != null
                && dueDate.isEqual(LocalDate.now().plusDays(1));
    }
}