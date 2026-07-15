package com.surtiventas.backend.user;

import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public List<User> findActiveByRole(Role role) {
        return userRepository.findByRoleAndActiveTrue(role);
    }

    public List<User> findAll() {
        return userRepository.findAllByOrderByActiveDescFullNameAsc();
    }

    @Transactional
    public User update(Long id, String fullName, Role role, boolean active) {
        User user = findById(id);
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(active);
        return userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String rawPassword) {
        User user = findById(id);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    @Transactional
    public User createUser(String email, String rawPassword, String fullName, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "A user with this email already exists");
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .role(role)
                .active(true)
                .build();
        return userRepository.save(user);
    }
}
