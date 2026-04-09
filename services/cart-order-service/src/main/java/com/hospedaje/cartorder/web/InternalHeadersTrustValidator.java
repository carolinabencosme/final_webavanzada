package com.hospedaje.cartorder.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InternalHeadersTrustValidator {
    private static final String INTERNAL_AUTH_HEADER = "X-Internal-Auth";

    @Value("${internal.auth.token}")
    private String internalAuthToken;

    public void validateOrThrow(HttpServletRequest request) {
        String providedToken = request.getHeader(INTERNAL_AUTH_HEADER);
        if (!internalAuthToken.equals(providedToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Untrusted identity headers source");
        }
    }

    public boolean isTrusted(HttpServletRequest request) {
        String providedToken = request.getHeader(INTERNAL_AUTH_HEADER);
        return internalAuthToken.equals(providedToken);
    }
}
