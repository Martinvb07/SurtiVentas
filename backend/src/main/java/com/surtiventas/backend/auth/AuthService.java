package com.surtiventas.backend.auth;

import com.surtiventas.backend.auth.dto.AuthResponse;
import com.surtiventas.backend.auth.dto.LoginRequest;
import com.surtiventas.backend.auth.dto.RegisterRequest;
import com.surtiventas.backend.auth.dto.UserSummary;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.security.jwt.JwtService;
import com.surtiventas.backend.user.User;
import com.surtiventas.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return buildAuthResponse(userDetails.getUser());
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshTokenService.RotationResult result = refreshTokenService.rotate(rawRefreshToken);
        String accessToken = jwtService.generateAccessToken(new CustomUserDetails(result.user()));
        return new AuthResponse(accessToken, result.rawToken(), jwtService.getAccessTokenExpirationMs(),
                UserSummary.from(result.user()));
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    @Transactional
    public UserSummary register(RegisterRequest request) {
        User user = userService.createUser(request.email(), request.password(), request.fullName(), request.role());
        return UserSummary.from(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.issue(user);
        return new AuthResponse(accessToken, refreshToken, jwtService.getAccessTokenExpirationMs(),
                UserSummary.from(user));
    }
}
