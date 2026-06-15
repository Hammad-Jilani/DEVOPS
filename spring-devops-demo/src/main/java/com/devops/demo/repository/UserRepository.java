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

    List<User> findAllByOrderByUsernameAsc();

    @Query("SELECT u FROM User u WHERE u.email IS NOT NULL AND u.email <> ''")
    List<User> findAllWithEmail();
}