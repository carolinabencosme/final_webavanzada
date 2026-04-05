package com.hospedaje.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 80)
    private String username;

    @Size(max = 255)
    private String email;

    private String currentPassword;

    @Size(min = 8, max = 128)
    private String newPassword;
}
