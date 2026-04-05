package com.hospedaje.auth.dto;

import com.hospedaje.auth.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateUserRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    /** When null, defaults to CLIENT. */
    private Role role;

    /** When null, defaults to true. */
    private Boolean active;

    /** When null, defaults to true (same welcome email as self-registration). */
    private Boolean sendWelcomeEmail;
}
