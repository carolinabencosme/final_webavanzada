package com.hospedaje.catalog.dto;
import lombok.*;
import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PageResponse<T> {
    private List<T> content;
    private int page, size;
    private long totalElements;
    private int totalPages;
}
