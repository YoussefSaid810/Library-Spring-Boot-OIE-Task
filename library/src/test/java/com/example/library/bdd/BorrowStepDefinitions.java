package com.example.library.bdd;

import com.example.library.model.Book;
import com.example.library.model.BorrowRecord;
import com.example.library.model.BorrowStatus;
import com.example.library.service.BookService;
import com.example.library.service.BorrowService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BorrowStepDefinitions {

    private final BookService bookService;
    private final BorrowService borrowService;

    private Book lastBorrowedBook;
    private BorrowRecord lastBorrowRecord;
    private boolean lastBorrowSuccess;
    private boolean lastReturnSuccess;
    private String lastErrorMessage;
    private List<BorrowRecord> lastQueriedRecords;

    public BorrowStepDefinitions(BookService bookService, BorrowService borrowService) {
        this.bookService = bookService;
        this.borrowService = borrowService;
    }

    @Given("the library has an available book with title {string}")
    public void theLibraryHasAnAvailableBook(String title) {
        lastBorrowedBook = bookService.addBook(new Book(null, title, "Author", "Category", true));
    }

    @Given("user {long} has borrowed the book")
    public void userHasBorrowedTheBook(long userId) {
        if (lastBorrowedBook == null) {
            lastBorrowedBook = bookService.addBook(new Book(null, "Auto Book", "Author", "Category", true));
        }
        lastBorrowRecord = borrowService.borrowBook(userId, lastBorrowedBook.id());
    }

    @Given("user {long} has borrowed a book")
    public void userHasBorrowedABook(long userId) {
        Book book = bookService.addBook(new Book(null, "Borrowed Book", "Author", "Category", true));
        lastBorrowRecord = borrowService.borrowBook(userId, book.id());
        lastBorrowedBook = book;
    }

    @Given("user {long} has {int} active borrowed books")
    public void userHasActiveBorrowedBooks(long userId, int count) {
        for (int i = 0; i < count; i++) {
            Book book = bookService.addBook(new Book(null, "Book " + i, "Author", "Category", true));
            borrowService.borrowBook(userId, book.id());
        }
    }

    @Given("user {long} has {int} overdue books")
    public void userHasOverdueBooks(long userId, int count) {
        for (int i = 0; i < count; i++) {
            Book book = bookService.addBook(new Book(null, "Overdue Book " + i, "Author", "Category", true));
            borrowService.addOverdueRecord(userId, book.id());
        }
    }

    @Given("user {long} has returned all overdue books")
    public void userHasReturnedAllOverdueBooks(long userId) {
        List<BorrowRecord> records = borrowService.getAllBorrows();
        for (BorrowRecord r : records) {
            if (r.userId().equals(userId) && r.status() == BorrowStatus.OVERDUE) {
                bookService.setBookAvailability(r.bookId(), true);
            }
        }
    }

    @Given("user {long} has returned the book")
    public void userHasReturnedTheBook(long userId) {
        if (lastBorrowRecord != null) {
            borrowService.returnBook(lastBorrowRecord.borrowId());
        }
    }

    @When("user {long} borrows the book")
    public void userBorrowsTheBook(long userId) {
        try {
            lastBorrowRecord = borrowService.borrowBook(userId, lastBorrowedBook.id());
            lastBorrowSuccess = true;
        } catch (IllegalStateException | IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            lastBorrowSuccess = false;
        }
    }

    @When("user {long} borrows the book with title {string}")
    public void userBorrowsTheBookWithTitle(long userId, String title) {
        Optional<Book> book = bookService.getAllBooks().stream()
            .filter(b -> b.title().equals(title))
            .findFirst();
        assertTrue(book.isPresent(), "Book not found: " + title);

        try {
            lastBorrowRecord = borrowService.borrowBook(userId, book.get().id());
            lastBorrowSuccess = true;
        } catch (IllegalStateException | IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            lastBorrowSuccess = false;
        }
    }

    @When("user {long} returns the book")
    public void userReturnsTheBook(long userId) {
        if (lastBorrowRecord != null) {
            Optional<BorrowRecord> result = borrowService.returnBook(lastBorrowRecord.borrowId());
            lastReturnSuccess = result.isPresent();
            if (result.isPresent()) {
                lastBorrowRecord = result.get();
            }
        } else {
            lastReturnSuccess = false;
        }
    }

    @When("user {long} returns a borrow record with id {long}")
    public void userReturnsBorrowRecordWithId(long userId, long borrowId) {
        Optional<BorrowRecord> result = borrowService.returnBook(borrowId);
        lastReturnSuccess = result.isPresent();
        if (result.isPresent()) {
            lastBorrowRecord = result.get();
        }
    }

    @When("I check all borrow records")
    public void iCheckAllBorrowRecords() {
        lastQueriedRecords = borrowService.getAllBorrows();
    }

    @Then("the borrow should succeed")
    public void theBorrowShouldSucceed() {
        assertTrue(lastBorrowSuccess);
        assertNotNull(lastBorrowRecord);
    }

    @Then("the borrow should fail with {string} error")
    public void theBorrowShouldFailWith(String errorMessage) {
        assertFalse(lastBorrowSuccess);
        assertNotNull(lastErrorMessage);
        assertTrue(lastErrorMessage.contains(errorMessage));
    }

    @And("the book should no longer be available")
    public void theBookShouldNoLongerBeAvailable() {
        Optional<Book> book = bookService.getBookById(lastBorrowedBook.id());
        assertTrue(book.isPresent());
        assertFalse(book.get().available());
    }

    @And("the borrow record should have status {string}")
    public void theBorrowRecordShouldHaveStatus(String status) {
        assertNotNull(lastBorrowRecord);
        assertEquals(BorrowStatus.valueOf(status), lastBorrowRecord.status());
    }

    @And("the due date should be 14 days from today")
    public void theDueDateShouldBe14DaysFromToday() {
        assertNotNull(lastBorrowRecord);
        assertEquals(LocalDate.now().plusDays(14), lastBorrowRecord.dueDate());
    }

    @Then("the return should succeed")
    public void theReturnShouldSucceed() {
        assertTrue(lastReturnSuccess);
    }

    @Then("the return should fail")
    public void theReturnShouldFail() {
        assertFalse(lastReturnSuccess);
    }

    @And("the book should be available again")
    public void theBookShouldBeAvailableAgain() {
        Optional<Book> book = bookService.getBookById(lastBorrowedBook.id());
        assertTrue(book.isPresent());
        assertTrue(book.get().available());
    }

    @And("the return date should be set to today")
    public void theReturnDateShouldBeSetToToday() {
        assertNotNull(lastBorrowRecord);
        assertEquals(LocalDate.now(), lastBorrowRecord.returnDate());
    }

    @Then("both borrows should succeed")
    public void bothBorrowsShouldSucceed() {
        assertTrue(lastBorrowSuccess);
    }

    @Then("there should be {int} records with status {string}")
    public void thereShouldBeRecordsWithStatus(int count, String status) {
        assertNotNull(lastQueriedRecords);
        long actual = lastQueriedRecords.stream()
            .filter(r -> r.status() == BorrowStatus.valueOf(status))
            .count();
        assertEquals(count, actual);
    }
}
