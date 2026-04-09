package com.hospedaje.cartorder.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class RequestIdentityResolver {
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    private final InternalHeadersTrustValidator internalHeadersTrustValidator;

    public RequestIdentity resolveFromHeaders(HttpServletRequest request) {
        internalHeadersTrustValidator.validateOrThrow(request);
        String userId = trimToNull(request.getHeader(USER_ID_HEADER));
        if (userId == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Missing required identity header 'X-User-Id'"
            );
        }
        String userEmail = trimToNull(request.getHeader(USER_EMAIL_HEADER));
        return new RequestIdentity(userId, userEmail);
    }

    public String resolveUserId(HttpServletRequest request) {
        return resolveFromHeaders(request).userId();
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record RequestIdentity(String userId, String userEmail) {
    }
}
