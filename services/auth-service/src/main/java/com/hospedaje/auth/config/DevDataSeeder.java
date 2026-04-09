package com.hospedaje.auth.config;

import com.hospedaje.auth.entity.Role;
import com.hospedaje.auth.entity.User;
import com.hospedaje.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createUserIfNotExists("alice", "alice@test.com", "password123", Role.CLIENT);
        createUserIfNotExists("bob", "bob@test.com", "password123", Role.CLIENT);
        createUserIfNotExists("carol", "carol@test.com", "password123", Role.CLIENT);
        log.info("Dev demo data seeding complete");
    }

    private void createUserIfNotExists(String username, String email, String password, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                .username(username).email(email)
                .password(passwordEncoder.encode(password))
                .role(role).active(true).build();
            userRepository.save(user);
            log.info("Created dev user: {}", email);
        }
    }
}
