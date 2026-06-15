package com.devops.demo.repository;

import com.devops.demo.model.Task;
import com.devops.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.assignee a
        WHERE
          (:search IS NULL OR :search = ''
           OR LOWER(t.title)       LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:priority IS NULL OR :priority = '' OR t.priority = :priority)
          AND (:status IS NULL OR :status = ''
               OR (:status = 'COMPLETED' AND t.completed = true)
               OR (:status = 'PENDING'   AND t.completed = false))
          AND (:assigneeUsername IS NULL OR :assigneeUsername = ''
               OR a.username = :assigneeUsername)
          AND (:viewerUsername IS NULL OR :viewerUsername = ''
               OR a.username = :viewerUsername)
        ORDER BY
          CASE t.priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END,
          t.dueDate ASC NULLS LAST,
          t.id ASC
    """)
    List<Task> search(
            @Param("search")           String search,
            @Param("priority")         String priority,
            @Param("status")           String status,
            @Param("assigneeUsername") String assigneeUsername,
            @Param("viewerUsername")   String viewerUsername
    );
    @Query("""
        SELECT t FROM Task t
        JOIN FETCH t.assignee a
        WHERE t.completed = false
          AND t.dueDate   = :tomorrow
          AND a.email IS NOT NULL
          AND a.email <> ''
    """)
    List<Task> findTasksDueTomorrowWithAssignee(@Param("tomorrow") LocalDate tomorrow);

    List<Task> findByAssignee(User assignee);

    long countByCompleted(boolean completed);
}