package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.model.BorrowRecord;
import com.example.library.model.BorrowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowService {

    private static final Logger log = LoggerFactory.getLogger(BorrowService.class);
    private static final int MAX_BORROWED = 5;
    private static final int MAX_OVERDUE = 3;
    private static final int BORROW_DAYS = 14;

    private final BookService bookService;
    private final List<BorrowRecord> borrowRecords = new ArrayList<>();
    private Long nextBorrowId = 1L;

    public BorrowService(BookService bookService) {
        this.bookService = bookService;
    }

    public BorrowRecord borrowBook(Long userId, Long bookId) {
        log.info("User {} attempting to borrow book {}", userId, bookId);

        Book book = bookService.getBookById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        if (!book.available()) {
            throw new IllegalStateException("Book is not available: " + bookId);
        }

        long activeBorrows = borrowRecords.stream()
            .filter(r -> r.userId().equals(userId) && r.isActive())
            .count();

        if (activeBorrows >= MAX_BORROWED) {
            throw new IllegalStateException("User has reached the maximum of " + MAX_BORROWED + " borrowed books");
        }

        refreshOverdueStatuses();

        long overdueBooks = borrowRecords.stream()
            .filter(r -> r.userId().equals(userId) && r.status() == BorrowStatus.OVERDUE)
            .count();

        if (overdueBooks >= MAX_OVERDUE) {
            throw new IllegalStateException("User has " + overdueBooks + " overdue books. Borrowing blocked.");
        }

        LocalDate now = LocalDate.now();
        BorrowRecord record = new BorrowRecord(
            nextBorrowId++,
            bookId,
            userId,
            now,
            now.plusDays(BORROW_DAYS),
            null,
            BorrowStatus.BORROWED
        );
        borrowRecords.add(record);

        bookService.setBookAvailability(bookId, false);

        log.info("Book {} borrowed by user {}, due: {}", bookId, userId, record.dueDate());
        return record;
    }

    public Optional<BorrowRecord> returnBook(Long borrowId) {
        log.info("Returning borrow record {}", borrowId);

        return borrowRecords.stream()
            .filter(r -> r.borrowId().equals(borrowId) && r.isActive())
            .findFirst()
            .map(record -> {
                BorrowRecord returned = new BorrowRecord(
                    record.borrowId(),
                    record.bookId(),
                    record.userId(),
                    record.borrowDate(),
                    record.dueDate(),
                    LocalDate.now(),
                    BorrowStatus.RETURNED
                );
                borrowRecords.remove(record);
                borrowRecords.add(returned);

                bookService.setBookAvailability(record.bookId(), true);

                log.info("Book {} returned by user {}", record.bookId(), record.userId());
                return returned;
            });
    }

    public List<BorrowRecord> getActiveBorrowsByUser(Long userId) {
        return borrowRecords.stream()
            .filter(r -> r.userId().equals(userId) && r.isActive())
            .toList();
    }

    public List<BorrowRecord> getAllBorrows() {
        refreshOverdueStatuses();
        return new ArrayList<>(borrowRecords);
    }

    private void refreshOverdueStatuses() {
        LocalDate now = LocalDate.now();
        for (int i = 0; i < borrowRecords.size(); i++) {
            BorrowRecord r = borrowRecords.get(i);
            if (r.status() == BorrowStatus.BORROWED && now.isAfter(r.dueDate())) {
                borrowRecords.set(i, new BorrowRecord(
                    r.borrowId(), r.bookId(), r.userId(),
                    r.borrowDate(), r.dueDate(), r.returnDate(),
                    BorrowStatus.OVERDUE
                ));
                log.info("Marked borrow {} as OVERDUE", r.borrowId());
            }
        }
    }

    public void addOverdueRecord(Long userId, Long bookId) {
        BorrowRecord record = new BorrowRecord(
            nextBorrowId++,
            bookId,
            userId,
            LocalDate.now().minusDays(30),
            LocalDate.now().minusDays(16),
            null,
            BorrowStatus.OVERDUE
        );
        borrowRecords.add(record);
        bookService.setBookAvailability(bookId, true);
    }
}
