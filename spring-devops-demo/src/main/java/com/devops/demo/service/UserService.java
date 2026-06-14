package com.devops.demo.service;

import com.devops.demo.model.User;
import com.devops.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── Seed a default admin so the app is immediately usable ────────────────

    @PostConstruct
    public void seedAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(new User(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "ROLE_ADMIN"
            ));
        }
    }

    // ── Spring Security hook ──────────────────────────────────────────────────

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

    /**
     * @return the saved user
     * @throws IllegalArgumentException if the username is already taken
     */
    @Transactional
    public User register(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username \"" + username + "\" is already taken.");
        }
        return userRepository.save(new User(
                username,
                passwordEncoder.encode(rawPassword),
                "ROLE_USER"
        ));
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
}