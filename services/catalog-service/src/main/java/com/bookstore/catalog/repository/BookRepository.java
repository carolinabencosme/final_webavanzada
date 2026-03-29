package com.bookstore.catalog.repository;
import com.bookstore.catalog.document.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
public interface BookRepository extends MongoRepository<Book, String> {
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    Page<Book> findByGenreIgnoreCase(String genre, Pageable pageable);
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author, Pageable pageable);
    @Query(value="{}", fields="{'genre': 1}")
    List<Book> findAllGenreFields();
}
