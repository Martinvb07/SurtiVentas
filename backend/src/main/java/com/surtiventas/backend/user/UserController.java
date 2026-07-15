package com.surtiventas.backend.user;

import com.surtiventas.backend.user.dto.ResetPasswordRequest;
import com.surtiventas.backend.user.dto.UserCreateRequest;
import com.surtiventas.backend.user.dto.UserResponse;
import com.surtiventas.backend.user.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** User accounts (login). Admin only. */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /** All users when no role is given; active users of a role otherwise (e.g. drivers). */
    @GetMapping
    public ResponseEntity<List<UserResponse>> search(@RequestParam(required = false) Role role) {
        List<User> users = (role != null) ? userService.findActiveByRole(role) : userService.findAll();
        return ResponseEntity.ok(users.stream().map(userMapper::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.createUser(request.email(), request.password(), request.fullName(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        User user = userService.update(id, request.fullName(), request.role(), request.active());
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.password());
        return ResponseEntity.noContent().build();
    }
}
