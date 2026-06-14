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

    /** Optional due date — null means no deadline. */
    private LocalDate dueDate;

    /**
     * The user this task is assigned to.
     * Null = unassigned (only admin sees it, no reminder sent).
     * LAZY fetch prevents N+1 on list pages; explicit join used in queries.
     */
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

    // ── Derived helpers — @Transient, computed at runtime ────────────────────

    @Transient
    public boolean isOverdue() {
        return !completed && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    @Transient
    public boolean isDueToday() {
        return !completed && dueDate != null && dueDate.isEqual(LocalDate.now());
    }

    /** True when dueDate is exactly tomorrow — used to trigger email reminders. */
    @Transient
    public boolean isDueTomorrow() {
        return !completed && dueDate != null
                && dueDate.isEqual(LocalDate.now().plusDays(1));
    }
}