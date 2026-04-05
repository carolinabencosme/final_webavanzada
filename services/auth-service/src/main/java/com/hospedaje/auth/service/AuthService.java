package com.hospedaje.auth.service;

import com.hospedaje.auth.dto.*;
import com.hospedaje.auth.entity.Role;
import com.hospedaje.auth.entity.User;
import com.hospedaje.auth.event.UserRegisteredEvent;
import com.hospedaje.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserEventPublisher eventPublisher;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already in use");
        }
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.CLIENT)
            .active(true)
            .build();
        user = userRepository.save(user);

        eventPublisher.publishUserRegistered(new UserRegisteredEvent(
            user.getId().toString(), user.getEmail(), user.getUsername(), LocalDateTime.now()
        ));

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
            .token(token).userId(user.getId().toString())
            .username(user.getUsername()).email(user.getEmail())
            .role(user.getRole().name()).build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!user.isActive()) {
            throw new RuntimeException("Account is disabled");
        }
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
            .token(token).userId(user.getId().toString())
            .username(user.getUsername()).email(user.getEmail())
            .role(user.getRole().name()).build();
    }

    public UserDto getUserById(String id) {
        User user = userRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("User not found"));
        return toDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public UserDto updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getActive() != null) user.setActive(request.getActive());
        return toDto(userRepository.save(user));
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    public UserDto updateOwnProfile(UUID id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String u = request.getUsername().trim();
            if (!u.equals(user.getUsername()) && userRepository.existsByUsername(u)) {
                throw new RuntimeException("Username already in use");
            }
            user.setUsername(u);
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String e = request.getEmail().trim();
            if (!e.contains("@")) {
                throw new RuntimeException("Invalid email");
            }
            if (!e.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(e)) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(e);
        }
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            String cur = request.getCurrentPassword();
            if (cur == null || !passwordEncoder.matches(cur, user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        return toDto(userRepository.save(user));
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
            .id(user.getId()).username(user.getUsername())
            .email(user.getEmail()).role(user.getRole())
            .active(user.isActive()).createdAt(user.getCreatedAt()).build();
    }
}
