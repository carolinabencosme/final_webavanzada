package com.hospedaje.auth.controller;

import com.hospedaje.auth.dto.*;
import com.hospedaje.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", authService.getAllUsers()));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateMe(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated", authService.updateOwnProfile(UUID.fromString(userId), request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> update(@PathVariable UUID id, @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated", authService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        authService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }
}
