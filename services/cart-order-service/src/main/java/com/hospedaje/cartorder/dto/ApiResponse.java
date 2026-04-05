package com.hospedaje.cartorder.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String code;
    private Object details;
    public static <T> ApiResponse<T> success(String msg, T data) { return ApiResponse.<T>builder().success(true).message(msg).data(data).build(); }
    public static <T> ApiResponse<T> error(String msg) { return ApiResponse.<T>builder().success(false).message(msg).build(); }
    public static <T> ApiResponse<T> error(String msg, String code, Object details) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(msg)
            .code(code)
            .details(details)
            .build();
    }
}
