package com.devops.demo.repository;

import com.devops.demo.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Full-text search across title + description, with optional priority filter.
     *
     * When a param is null or blank the condition is skipped so the same query
     * handles every combination of filter/search the UI can produce.
     */
    @Query("""
        SELECT t FROM Task t
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(t.title)       LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:priority IS NULL OR :priority = '' OR t.priority = :priority)
          AND (:status   IS NULL OR :status   = ''
               OR (:status = 'COMPLETED'  AND t.completed = true)
               OR (:status = 'PENDING'    AND t.completed = false))
        ORDER BY
            CASE t.priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END,
            t.dueDate ASC NULLS LAST,
            t.id ASC
    """)
    List<Task> search(
            @Param("search")   String search,
            @Param("priority") String priority,
            @Param("status")   String status
    );

    long countByCompleted(boolean completed);
}