package com.devops.demo.repository;

import com.devops.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /** All users ordered by username — used to populate the assignee dropdown. */
    List<User> findAllByOrderByUsernameAsc();

    /**
     * Users who have a non-null email — the only ones we can send reminders to.
     * Used by the reminder scheduler to fetch candidates efficiently.
     */
    @Query("SELECT u FROM User u WHERE u.email IS NOT NULL AND u.email <> ''")
    List<User> findAllWithEmail();
}