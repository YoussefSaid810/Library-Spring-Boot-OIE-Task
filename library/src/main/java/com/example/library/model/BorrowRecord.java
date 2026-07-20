package com.example.library.model;

import java.time.LocalDate;

public record BorrowRecord(
    Long borrowId,
    Long bookId,
    Long userId,
    LocalDate borrowDate,
    LocalDate dueDate,
    LocalDate returnDate,
    BorrowStatus status
) {
    public boolean isOverdue() {
        return status == BorrowStatus.BORROWED && LocalDate.now().isAfter(dueDate);
    }

    public boolean isActive() {
        return status == BorrowStatus.BORROWED;
    }
}
