package com.bookstore.review.dto;
import lombok.*;
import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor
public class OrderDto {
    private Long id;
    private String userId, status;
    private List<OrderItemDto> items;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemDto {
        private String bookId;
    }
}
