package com.bookstore.catalog.controller;
import com.bookstore.catalog.dto.*;
import com.bookstore.catalog.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("/books")
    public ResponseEntity<ApiResponse<PageResponse<BookDto>>> getBooks(
        @RequestParam(defaultValue="0") int page,
        @RequestParam(defaultValue="12") int size,
        @RequestParam(required=false) String genre) {
        return ResponseEntity.ok(ApiResponse.success("OK", bookService.getBooks(page, size, genre)));
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<ApiResponse<BookDto>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("OK", bookService.getBookById(id)));
    }

    @GetMapping("/books/search")
    public ResponseEntity<ApiResponse<PageResponse<BookDto>>> search(
        @RequestParam(required=false) String title,
        @RequestParam(required=false) String author,
        @RequestParam(required=false) String genre,
        @RequestParam(defaultValue="0") int page,
        @RequestParam(defaultValue="12") int size) {
        return ResponseEntity.ok(ApiResponse.success("OK", bookService.searchBooks(title, author, genre, page, size)));
    }

    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<String>>> getGenres() {
        return ResponseEntity.ok(ApiResponse.success("OK", bookService.getGenres()));
    }

    @PostMapping("/books/batch")
    public ResponseEntity<ApiResponse<List<BookDto>>> getBatch(@RequestBody List<String> ids) {
        return ResponseEntity.ok(ApiResponse.success("OK", bookService.getBooksByIds(ids)));
    }

    @PutMapping("/books/{id}/rating")
    public ResponseEntity<ApiResponse<Void>> updateRating(@PathVariable String id,
        @RequestParam double averageRating, @RequestParam int totalReviews) {
        bookService.updateBookRating(id, averageRating, totalReviews);
        return ResponseEntity.ok(ApiResponse.success("Rating updated", null));
    }
}
