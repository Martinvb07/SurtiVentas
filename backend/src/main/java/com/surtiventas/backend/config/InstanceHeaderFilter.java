package com.surtiventas.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Stamps every response with the id of the backend instance that served it
 * ({@code X-Backend-Instance}). With several instances behind the load balancer
 * (HA), this makes the round-robin observable and helps trace which instance
 * handled a request.
 */
@Component
public class InstanceHeaderFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Backend-Instance";
    private final String instanceId = resolveInstanceId();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader(HEADER, instanceId);
        chain.doFilter(request, response);
    }

    private static String resolveInstanceId() {
        String fromEnv = System.getenv("HOSTNAME");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return "unknown";
        }
    }
}
