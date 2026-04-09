package com.hospedaje.cartorder.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminRoleInterceptor implements HandlerInterceptor {
    private static final String ROLE_HEADER = "X-User-Role";
    private final InternalHeadersTrustValidator internalHeadersTrustValidator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!internalHeadersTrustValidator.isTrusted(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        String role = request.getHeader(ROLE_HEADER);
        if (role != null && "ADMIN".equalsIgnoreCase(role.trim())) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
}
