package com.hospedaje.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {
    private static final String INTERNAL_AUTH_HEADER = "X-Internal-Auth";

    @Value("${internal.auth.token}")
    private String internalAuthToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        String userEmail = request.getHeader("X-User-Email");
        boolean hasIdentityHeaders = hasText(userId) || hasText(userRole) || hasText(userEmail);

        if (hasIdentityHeaders && !internalAuthToken.equals(request.getHeader(INTERNAL_AUTH_HEADER))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Untrusted identity headers source");
            return;
        }

        if (userId != null && !userId.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
            String role = userRole != null ? userRole : "CLIENT";
            if (!role.startsWith("ROLE_")) role = "ROLE_" + role;
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority(role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
