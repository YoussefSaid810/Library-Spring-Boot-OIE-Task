package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.model.BorrowRecord;
import com.example.library.model.BorrowStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BorrowServiceTest {

    private BookService bookService;
    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        bookService = new BookService();
        borrowService = new BorrowService(bookService);

        bookService.addBook(new Book(null, "Book 1", "Author 1", "Cat 1", true));
        bookService.addBook(new Book(null, "Book 2", "Author 2", "Cat 2", true));
        bookService.addBook(new Book(null, "Book 3", "Author 3", "Cat 3", true));
    }

    @Test
    @DisplayName("Should borrow an available book")
    void shouldBorrowBook() {
        BorrowRecord record = borrowService.borrowBook(1L, 1L);

        assertNotNull(record.borrowId());
        assertEquals(1L, record.userId());
        assertEquals(1L, record.bookId());
        assertEquals(BorrowStatus.BORROWED, record.status());
        assertEquals(LocalDate.now(), record.borrowDate());
        assertEquals(LocalDate.now().plusDays(14), record.dueDate());
        assertNull(record.returnDate());

        Optional<Book> book = bookService.getBookById(1L);
        assertTrue(book.isPresent());
        assertFalse(book.get().available());
    }

    @Test
    @DisplayName("Should reject borrow when book is not available")
    void shouldRejectBorrowWhenBookNotAvailable() {
        borrowService.borrowBook(1L, 1L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> borrowService.borrowBook(2L, 1L));

        assertEquals("Book is not available: 1", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject borrow when user has 5 active borrows")
    void shouldRejectBorrowWhenMaxReached() {
        bookService.addBook(new Book(null, "Book 4", "Author 4", "Cat 4", true));
        bookService.addBook(new Book(null, "Book 5", "Author 5", "Cat 5", true));
        bookService.addBook(new Book(null, "Book 6", "Author 6", "Cat 6", true));

        borrowService.borrowBook(1L, 1L);
        borrowService.borrowBook(1L, 2L);
        borrowService.borrowBook(1L, 3L);
        borrowService.borrowBook(1L, 4L);
        borrowService.borrowBook(1L, 5L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> borrowService.borrowBook(1L, 6L));

        assertEquals("User has reached the maximum of 5 borrowed books", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject borrow when user has 3 or more overdue books")
    void shouldRejectBorrowWhenOverdue() {
        bookService.addBook(new Book(null, "Book 4", "Author 4", "Cat 4", true));
        bookService.addBook(new Book(null, "Book 5", "Author 5", "Cat 5", true));
        bookService.addBook(new Book(null, "Book 6", "Author 6", "Cat 6", true));

        borrowService.addOverdueRecord(1L, 1L);
        borrowService.addOverdueRecord(1L, 2L);
        borrowService.addOverdueRecord(1L, 3L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> borrowService.borrowBook(1L, 4L));

        assertTrue(ex.getMessage().contains("overdue"));
    }

    @Test
    @DisplayName("Should return a borrowed book")
    void shouldReturnBook() {
        BorrowRecord borrowed = borrowService.borrowBook(1L, 1L);

        Optional<BorrowRecord> returned = borrowService.returnBook(borrowed.borrowId());

        assertTrue(returned.isPresent());
        assertEquals(BorrowStatus.RETURNED, returned.get().status());
        assertEquals(LocalDate.now(), returned.get().returnDate());

        Optional<Book> book = bookService.getBookById(1L);
        assertTrue(book.isPresent());
        assertTrue(book.get().available());
    }

    @Test
    @DisplayName("Should return empty when returning non-existent record")
    void shouldReturnEmptyWhenRecordNotFound() {
        Optional<BorrowRecord> result = borrowService.returnBook(999L);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should get active borrows by user")
    void shouldGetActiveBorrowsByUser() {
        borrowService.borrowBook(1L, 1L);
        borrowService.borrowBook(1L, 2L);
        borrowService.borrowBook(2L, 3L);

        List<BorrowRecord> user1 = borrowService.getActiveBorrowsByUser(1L);
        assertEquals(2, user1.size());

        List<BorrowRecord> user2 = borrowService.getActiveBorrowsByUser(2L);
        assertEquals(1, user2.size());
    }

    @Test
    @DisplayName("Should allow different users to borrow same book after return")
    void shouldAllowReborrowAfterReturn() {
        BorrowRecord r1 = borrowService.borrowBook(1L, 1L);
        borrowService.returnBook(r1.borrowId());

        BorrowRecord r2 = borrowService.borrowBook(2L, 1L);

        assertNotNull(r2);
        assertEquals(2L, r2.userId());
        assertEquals(1L, r2.bookId());
    }

    @Test
    @DisplayName("Should detect overdue records via refreshOverdueStatuses")
    void shouldDetectOverdueRecords() {
        borrowService.addOverdueRecord(1L, 1L);
        borrowService.addOverdueRecord(1L, 2L);

        List<BorrowRecord> all = borrowService.getAllBorrows();

        long overdueCount = all.stream()
            .filter(r -> r.status() == BorrowStatus.OVERDUE)
            .count();
        assertEquals(2, overdueCount);
    }

    @Test
    @DisplayName("Should not allow return of already returned record")
    void shouldNotAllowDoubleReturn() {
        BorrowRecord borrowed = borrowService.borrowBook(1L, 1L);
        borrowService.returnBook(borrowed.borrowId());

        Optional<BorrowRecord> secondReturn = borrowService.returnBook(borrowed.borrowId());
        assertFalse(secondReturn.isPresent());
    }
}
