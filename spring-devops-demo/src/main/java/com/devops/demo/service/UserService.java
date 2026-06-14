package com.devops.demo.service;

import com.devops.demo.model.User;
import com.devops.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired private UserRepository userRepository;
    @Autowired @Lazy private PasswordEncoder passwordEncoder;

    // ── Seed admin ───────────────────────────────────────────────────────────

    @PostConstruct
    public void seedAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin",
                    passwordEncoder.encode("admin123"),
                    "admin@devops.local",
                    "ROLE_ADMIN");
            userRepository.save(admin);
        }
    }

    // ── Spring Security ───────────────────────────────────────────────────────

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole()))
        );
    }

    // ── Registration ──────────────────────────────────────────────────────────

    @Transactional
    public User register(String username, String rawPassword, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username \"" + username + "\" is already taken.");
        }
        User user = new User(username, passwordEncoder.encode(rawPassword), "ROLE_USER");
        if (email != null && !email.isBlank()) {
            user.setEmail(email.trim());
        }
        return userRepository.save(user);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}