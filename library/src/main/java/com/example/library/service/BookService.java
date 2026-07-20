package com.example.library.service;

import com.example.library.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private final List<Book> books = new ArrayList<>();
    private Long nextId = 1L;

    public List<Book> getAllBooks() {
        log.debug("Fetching all books, count: {}", books.size());
        return new ArrayList<>(books);
    }

    public Optional<Book> getBookById(Long id) {
        log.debug("Fetching book with id: {}", id);
        return books.stream()
            .filter(book -> book.id().equals(id))
            .findFirst();
    }

    public Book addBook(Book book) {
        log.info("Adding new book: {} by {}", book.title(), book.author());
        Book newBook = new Book(nextId++, book.title(), book.author(), book.category(), true);
        books.add(newBook);
        log.debug("Book added with id: {}", newBook.id());
        return newBook;
    }

    public Optional<Book> updateBook(Long id, Book updatedBook) {
        log.info("Updating book with id: {}", id);
        return getBookById(id).map(existing -> {
            Book book = new Book(id, updatedBook.title(), updatedBook.author(), updatedBook.category(), updatedBook.available());
            books.remove(existing);
            books.add(book);
            log.debug("Book updated: {}", book);
            return book;
        });
    }

    public Optional<Book> setBookAvailability(Long id, boolean available) {
        return getBookById(id).map(existing -> {
            Book book = new Book(id, existing.title(), existing.author(), existing.category(), available);
            books.remove(existing);
            books.add(book);
            log.debug("Book {} availability set to {}", id, available);
            return book;
        });
    }

    public boolean deleteBook(Long id) {
        log.info("Deleting book with id: {}", id);
        Optional<Book> book = getBookById(id);
        if (book.isEmpty()) {
            log.warn("Book with id {} not found for deletion", id);
            return false;
        }
        if (!book.get().available()) {
            throw new IllegalStateException("Cannot delete book " + id + ": book is currently borrowed");
        }
        books.remove(book.get());
        log.debug("Book deleted successfully");
        return true;
    }
}
