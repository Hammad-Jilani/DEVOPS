package com.devops.demo.config;

import com.devops.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * @Lazy breaks the circular dependency:
     * SecurityConfig → UserService → PasswordEncoder → SecurityConfig
     */
    @Autowired
    @Lazy
    private UserService userService;

    // ── BCrypt password encoder ───────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ── Auth provider wires UserService + BCrypt together ────────────────────

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── HTTP security rules ───────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth
                        // Public pages — login, register, static assets
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                        // Actuator health endpoint — open for load balancer checks
                        .requestMatchers("/actuator/health").permitAll()
                        // H2 console — dev only; restrict or remove in production
                        .requestMatchers("/h2-console/**").permitAll()
                        // Everything else requires a logged-in user
                        .anyRequest().authenticated()
                )

                // ── Custom login form ─────────────────────────────────────────
                .formLogin(form -> form
                        .loginPage("/login")                  // GET  /login  → our login.html
                        .loginProcessingUrl("/login")          // POST /login  → handled by Security
                        .defaultSuccessUrl("/", true)          // after login  → home
                        .failureUrl("/login?error=true")       // bad creds    → login?error
                        .permitAll()
                )

                // ── Logout ───────────────────────────────────────────────────
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // ── H2 console needs frames + relaxed CSRF ────────────────────
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}