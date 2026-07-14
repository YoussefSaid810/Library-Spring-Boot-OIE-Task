package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book(1L, "Spring in Action", "Craig Walls", "Programming");
    }

    @Test
    @DisplayName("GET /books should return list of books")
    void shouldReturnBooks() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(testBook));

        mockMvc.perform(get("/books"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Spring in Action")));
    }

    @Test
    @DisplayName("GET /books/{id} should return book")
    void shouldReturnBookById() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));

        mockMvc.perform(get("/books/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title", is("Spring in Action")));
    }

    @Test
    @DisplayName("GET /books/{id} should return 404 when not found")
    void shouldReturn404WhenNotFound() throws Exception {
        when(bookService.getBookById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /books should create book")
    void shouldCreateBook() throws Exception {
        when(bookService.addBook(any(Book.class))).thenReturn(testBook);

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Spring in Action\",\"author\":\"Craig Walls\",\"category\":\"Programming\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title", is("Spring in Action")));
    }

    @Test
    @DisplayName("PUT /books/{id} should update book")
    void shouldUpdateBook() throws Exception {
        when(bookService.updateBook(any(Long.class), any(Book.class))).thenReturn(Optional.of(testBook));

        mockMvc.perform(put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Title\",\"author\":\"Updated Author\",\"category\":\"Updated\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title", is("Spring in Action")));
    }

    @Test
    @DisplayName("DELETE /books/{id} should delete book")
    void shouldDeleteBook() throws Exception {
        when(bookService.deleteBook(1L)).thenReturn(true);

        mockMvc.perform(delete("/books/1"))
            .andExpect(status().isNoContent());
    }
}
