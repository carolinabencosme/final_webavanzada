package com.hospedaje.auth.config;

import com.hospedaje.auth.entity.Role;
import com.hospedaje.auth.entity.User;
import com.hospedaje.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createUserIfNotExists("admin", "admin@bookstore.com", "admin123", Role.ADMIN);
        createUserIfNotExists("alice", "alice@test.com", "password123", Role.CLIENT);
        createUserIfNotExists("bob", "bob@test.com", "password123", Role.CLIENT);
        createUserIfNotExists("carol", "carol@test.com", "password123", Role.CLIENT);
        log.info("Data initialization complete");
    }

    private void createUserIfNotExists(String username, String email, String password, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                .username(username).email(email)
                .password(passwordEncoder.encode(password))
                .role(role).active(true).build();
            userRepository.save(user);
            log.info("Created user: {}", email);
        }
    }
}
