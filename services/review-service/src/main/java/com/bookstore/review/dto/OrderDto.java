package com.bookstore.review.dto;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDto {
    @JsonAlias({"id", "orderId"})
    private Long id;
    @JsonAlias({"orderId", "id"})
    private Long orderId;
    private String userId, status;
    private List<OrderItemDto> items;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemDto {
        @JsonAlias({"bookId", "productId"})
        private String bookId;
    }
}
