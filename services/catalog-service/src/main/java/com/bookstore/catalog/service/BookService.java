package com.bookstore.catalog.service;
import com.bookstore.catalog.document.Book;
import com.bookstore.catalog.dto.*;
import com.bookstore.catalog.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.*;
@Service @RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public PageResponse<BookDto> getBooks(int page, int size, String genre) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title"));
        Page<Book> result = (genre != null && !genre.isBlank())
            ? bookRepository.findByGenreIgnoreCase(genre, pageable)
            : bookRepository.findAll(pageable);
        return toPageResponse(result);
    }

    public PageResponse<BookDto> searchBooks(String title, String author, String genre, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> result;
        if (title != null && !title.isBlank() && genre != null && !genre.isBlank()) {
            result = bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (title != null && !title.isBlank()) {
            result = bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (author != null && !author.isBlank()) {
            result = bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
        } else if (genre != null && !genre.isBlank()) {
            result = bookRepository.findByGenreIgnoreCase(genre, pageable);
        } else {
            result = bookRepository.findAll(pageable);
        }
        return toPageResponse(result);
    }

    public BookDto getBookById(String id) {
        Book b = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found: " + id));
        return toDto(b);
    }

    public List<String> getGenres() {
        return bookRepository.findAll().stream()
            .map(Book::getGenre).filter(Objects::nonNull)
            .distinct().sorted().collect(Collectors.toList());
    }

    public List<BookDto> getBooksByIds(List<String> ids) {
        return bookRepository.findAllById(ids).stream().map(this::toDto).collect(Collectors.toList());
    }

    public void updateBookRating(String bookId, double avgRating, int totalReviews) {
        bookRepository.findById(bookId).ifPresent(b -> {
            b.setAverageRating(avgRating);
            b.setTotalReviews(totalReviews);
            bookRepository.save(b);
        });
    }

    private PageResponse<BookDto> toPageResponse(Page<Book> page) {
        return PageResponse.<BookDto>builder()
            .content(page.getContent().stream().map(this::toDto).collect(Collectors.toList()))
            .page(page.getNumber()).size(page.getSize())
            .totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).build();
    }

    private BookDto toDto(Book b) {
        return BookDto.builder().id(b.getId()).title(b.getTitle()).author(b.getAuthor())
            .genre(b.getGenre()).description(b.getDescription()).price(b.getPrice())
            .coverUrl(b.getCoverUrl()).stock(b.getStock()).averageRating(b.getAverageRating())
            .totalReviews(b.getTotalReviews()).isbn(b.getIsbn())
            .publishedYear(b.getPublishedYear()).language(b.getLanguage()).pages(b.getPages()).build();
    }
}
