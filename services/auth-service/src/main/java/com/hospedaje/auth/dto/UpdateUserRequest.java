package com.hospedaje.auth.dto;

import com.hospedaje.auth.entity.Role;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String email;
    private Role role;
    private Boolean active;
}
