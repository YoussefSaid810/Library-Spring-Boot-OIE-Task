package com.example.library.service;

import com.example.library.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService();
    }

    @Test
    @DisplayName("Should add a book and return it with generated id")
    void shouldAddBook() {
        Book book = new Book(null, "Spring in Action", "Craig Walls", "Programming");
        
        Book added = bookService.addBook(book);
        
        assertNotNull(added.id());
        assertEquals("Spring in Action", added.title());
        assertEquals("Craig Walls", added.author());
        assertEquals("Programming", added.category());
    }

    @Test
    @DisplayName("Should get all books")
    void shouldGetAllBooks() {
        bookService.addBook(new Book(null, "Book 1", "Author 1", "Category 1"));
        bookService.addBook(new Book(null, "Book 2", "Author 2", "Category 2"));
        
        List<Book> books = bookService.getAllBooks();
        
        assertEquals(2, books.size());
    }

    @Test
    @DisplayName("Should get book by id")
    void shouldGetBookById() {
        Book added = bookService.addBook(new Book(null, "Test Book", "Test Author", "Test Category"));
        
        Optional<Book> found = bookService.getBookById(added.id());
        
        assertTrue(found.isPresent());
        assertEquals("Test Book", found.get().title());
    }

    @Test
    @DisplayName("Should return empty optional when book not found")
    void shouldReturnEmptyWhenBookNotFound() {
        Optional<Book> found = bookService.getBookById(999L);
        
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should update book")
    void shouldUpdateBook() {
        Book added = bookService.addBook(new Book(null, "Old Title", "Old Author", "Old Category"));
        Book updated = new Book(null, "New Title", "New Author", "New Category");
        
        Optional<Book> result = bookService.updateBook(added.id(), updated);
        
        assertTrue(result.isPresent());
        assertEquals("New Title", result.get().title());
        assertEquals("New Author", result.get().author());
    }

    @Test
    @DisplayName("Should return empty when updating non-existent book")
    void shouldReturnEmptyWhenUpdatingNonExistentBook() {
        Book updated = new Book(null, "New Title", "New Author", "New Category");
        
        Optional<Book> result = bookService.updateBook(999L, updated);
        
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should delete book")
    void shouldDeleteBook() {
        Book added = bookService.addBook(new Book(null, "To Delete", "Author", "Category"));
        
        boolean deleted = bookService.deleteBook(added.id());
        
        assertTrue(deleted);
        assertTrue(bookService.getAllBooks().isEmpty());
    }

    @Test
    @DisplayName("Should return false when deleting non-existent book")
    void shouldReturnFalseWhenDeletingNonExistentBook() {
        boolean deleted = bookService.deleteBook(999L);
        
        assertFalse(deleted);
    }
}
