package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book(1L, "Test Book", "Test Author", "Test Category");
    }

    @Test
    @DisplayName("Should return all books")
    void shouldReturnAllBooks() {
        when(bookService.getAllBooks()).thenReturn(List.of(testBook));

        List<Book> books = bookController.getAllBooks();

        assertEquals(1, books.size());
        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    @DisplayName("Should return book by id")
    void shouldReturnBookById() {
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));

        var response = bookController.getBookById(1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals("Test Book", response.getBody().title());
    }

    @Test
    @DisplayName("Should return 404 when book not found")
    void shouldReturn404WhenBookNotFound() {
        when(bookService.getBookById(999L)).thenReturn(Optional.empty());

        var response = bookController.getBookById(999L);

        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Should add book")
    void shouldAddBook() {
        Book newBook = new Book(null, "New Book", "New Author", "New Category");
        when(bookService.addBook(any(Book.class))).thenReturn(testBook);

        Book added = bookController.addBook(newBook);

        assertNotNull(added);
        assertEquals("Test Book", added.title());
        verify(bookService, times(1)).addBook(any(Book.class));
    }

    @Test
    @DisplayName("Should update book")
    void shouldUpdateBook() {
        Book updatedBook = new Book(null, "Updated", "Updated Author", "Updated Category");
        when(bookService.updateBook(eq(1L), any(Book.class))).thenReturn(Optional.of(testBook));

        var response = bookController.updateBook(1L, updatedBook);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(bookService, times(1)).updateBook(eq(1L), any(Book.class));
    }

    @Test
    @DisplayName("Should delete book")
    void shouldDeleteBook() {
        when(bookService.deleteBook(1L)).thenReturn(true);

        bookController.deleteBook(1L);

        verify(bookService, times(1)).deleteBook(1L);
    }
}
