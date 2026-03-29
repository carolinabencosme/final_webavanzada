package com.bookstore.auth.dto;

import com.bookstore.auth.entity.Role;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String email;
    private Role role;
    private Boolean active;
}
