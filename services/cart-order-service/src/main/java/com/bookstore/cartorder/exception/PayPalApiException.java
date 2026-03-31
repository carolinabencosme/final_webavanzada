package com.bookstore.cartorder.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PayPalApiException extends RuntimeException {
    private final String internalCode;
    private final String userMessage;
    private final HttpStatus upstreamStatus;
    private final String upstreamBodySnippet;

    public PayPalApiException(String internalCode, String userMessage, HttpStatus upstreamStatus, String upstreamBodySnippet) {
        super(userMessage);
        this.internalCode = internalCode;
        this.userMessage = userMessage;
        this.upstreamStatus = upstreamStatus;
        this.upstreamBodySnippet = upstreamBodySnippet;
    }
}
