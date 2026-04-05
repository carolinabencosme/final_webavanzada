package com.hospedaje.cartorder.exception;

import com.hospedaje.cartorder.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PayPalApiException.class)
    public ResponseEntity<ApiResponse<Void>> handlePayPal(PayPalApiException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("upstreamStatus", ex.getUpstreamStatus() != null ? ex.getUpstreamStatus().value() : null);
        details.put("upstreamBodySnippet", ex.getUpstreamBodySnippet());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(ApiResponse.error(ex.getUserMessage(), ex.getInternalCode(), details));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ex.getMessage(), "BAD_REQUEST", null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handle(RuntimeException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ex.getMessage(), "RUNTIME_ERROR", null));
    }
}
