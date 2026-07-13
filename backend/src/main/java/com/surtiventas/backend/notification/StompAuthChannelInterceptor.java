package com.surtiventas.backend.notification;

import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.security.CustomUserDetailsService;
import com.surtiventas.backend.security.jwt.JwtService;
import com.surtiventas.backend.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Authenticates the STOMP session from the JWT sent in the CONNECT frame (the
 * WebSocket handshake itself is anonymous), and restricts role topics so a user
 * can only subscribe to notifications for their own role.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_TOPIC_PREFIX = "/topic/notifications/";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            accessor.setUser(authenticate(accessor));
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscription(accessor);
        }
        return message;
    }

    private Authentication authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Falta el token de autenticación en la conexión");
        }
        String token = header.substring(BEARER_PREFIX.length());
        if (!jwtService.isTokenValid(token)) {
            throw new IllegalArgumentException("Token inválido o expirado");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractEmail(token));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(ROLE_TOPIC_PREFIX)) {
            return;
        }
        if (!(accessor.getUser() instanceof Authentication auth)
                || !(auth.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new IllegalArgumentException("No autenticado");
        }
        Role role = principal.getUser().getRole();
        String requestedRole = destination.substring(ROLE_TOPIC_PREFIX.length());
        if (role != Role.ADMINISTRADOR && !role.name().equals(requestedRole)) {
            throw new IllegalArgumentException("No autorizado para este canal de notificaciones");
        }
    }
}
