package com.surtiventas.backend.auth;

import com.surtiventas.backend.auth.dto.AuthResponse;
import com.surtiventas.backend.auth.dto.LoginRequest;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.security.jwt.JwtService;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import com.surtiventas.backend.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, userService, jwtService, refreshTokenService);
    }

    @Test
    void loginReturnsTokenPairOnValidCredentials() {
        User user = User.builder()
                .id(1L)
                .email("vendedor@surtiventas.com")
                .fullName("Vendedor Uno")
                .role(Role.VENDEDOR)
                .active(true)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(refreshTokenService.issue(user)).thenReturn("refresh-token");

        AuthResponse response = authService.login(new LoginRequest("vendedor@surtiventas.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().email()).isEqualTo("vendedor@surtiventas.com");
        assertThat(response.user().role()).isEqualTo(Role.VENDEDOR);
    }

    @Test
    void loginThrowsOnBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("vendedor@surtiventas.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
