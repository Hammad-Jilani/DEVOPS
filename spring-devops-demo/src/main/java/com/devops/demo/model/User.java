package com.devops.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Stored as a BCrypt hash — never plain text. */
    @Column(nullable = false)
    private String password;

    /**
     * Simple single-role model: "ROLE_USER" or "ROLE_ADMIN".
     * Spring Security expects the "ROLE_" prefix.
     */
    @Column(nullable = false, length = 20)
    private String role = "ROLE_USER";

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role     = role;
    }
}