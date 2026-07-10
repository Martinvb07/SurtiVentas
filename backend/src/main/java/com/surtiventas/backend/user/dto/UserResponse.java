package com.surtiventas.backend.user.dto;

import com.surtiventas.backend.user.Role;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        boolean active
) {
}
