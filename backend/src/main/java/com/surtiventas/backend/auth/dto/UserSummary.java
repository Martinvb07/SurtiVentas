package com.surtiventas.backend.auth.dto;

import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;

public record UserSummary(
        Long id,
        String email,
        String fullName,
        Role role
) {
    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
