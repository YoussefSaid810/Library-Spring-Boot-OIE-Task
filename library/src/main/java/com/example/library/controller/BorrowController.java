package com.example.library.controller;

import com.example.library.model.BorrowRecord;
import com.example.library.service.BorrowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    private static final Logger log = LoggerFactory.getLogger(BorrowController.class);
    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping
    public ResponseEntity<?> borrowBook(@RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Long bookId = request.get("bookId");
        log.info("POST /borrow - userId: {}, bookId: {}", userId, bookId);

        try {
            BorrowRecord record = borrowService.borrowBook(userId, bookId);
            return ResponseEntity.ok(record);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Borrow failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestBody Map<String, Long> request) {
        Long borrowRecordId = request.get("borrowRecordId");
        log.info("POST /return - borrowRecordId: {}", borrowRecordId);

        return borrowService.returnBook(borrowRecordId)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElse(ResponseEntity.badRequest().body(Map.of("error", "Active borrow record not found")));
    }

    @GetMapping("/user/{userId}")
    public List<BorrowRecord> getActiveBorrowsByUser(@PathVariable Long userId) {
        log.info("GET /borrow/user/{}", userId);
        return borrowService.getActiveBorrowsByUser(userId);
    }

    @GetMapping
    public List<BorrowRecord> getAllBorrows() {
        log.info("GET /borrow");
        return borrowService.getAllBorrows();
    }
}
