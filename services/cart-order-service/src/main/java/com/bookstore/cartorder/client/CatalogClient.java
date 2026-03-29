package com.bookstore.cartorder.client;
import com.bookstore.cartorder.dto.ApiResponse;
import com.bookstore.cartorder.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@FeignClient(name = "catalog-service")
public interface CatalogClient {
    @GetMapping("/books/{id}")
    ApiResponse<BookDto> getBook(@PathVariable String id);
    @PostMapping("/books/batch")
    ApiResponse<List<BookDto>> getBooks(@RequestBody List<String> ids);
}
