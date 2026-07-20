package com.example.library.bdd;

import com.example.library.model.Book;
import com.example.library.service.BookService;
import com.example.library.service.BorrowService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BookStepDefinitions {

    private final BookService bookService;
    private final BorrowService borrowService;

    private Book lastCreatedBook;
    private List<Book> lastQueriedBooks;
    private Optional<Book> lastQueriedBook;
    private boolean lastDeleteResult;
    private String lastErrorMessage;

    public BookStepDefinitions(BookService bookService, BorrowService borrowService) {
        this.bookService = bookService;
        this.borrowService = borrowService;
    }

    @Given("the library system is running")
    public void theLibrarySystemIsRunning() {
        assertNotNull(bookService);
        assertNotNull(borrowService);
    }

    @Given("the library has a book with title {string}, author {string}, category {string}")
    public void theLibraryHasABook(String title, String author, String category) {
        lastCreatedBook = bookService.addBook(new Book(null, title, author, category, true));
    }

    @When("I add a book with title {string}, author {string}, category {string}")
    public void iAddABook(String title, String author, String category) {
        lastCreatedBook = bookService.addBook(new Book(null, title, author, category, true));
    }

    @Then("the book should be created with a generated id")
    public void theBookShouldBeCreatedWithGeneratedId() {
        assertNotNull(lastCreatedBook);
        assertNotNull(lastCreatedBook.id());
    }

    @And("the book should be available")
    public void theBookShouldBeAvailable() {
        assertTrue(lastCreatedBook.available());
    }

    @And("the library should have {int} books")
    public void theLibraryShouldHaveBooks(int count) {
        assertEquals(count, bookService.getAllBooks().size());
    }

    @When("I request all books")
    public void iRequestAllBooks() {
        lastQueriedBooks = bookService.getAllBooks();
    }

    @Then("I should receive {int} books")
    public void iShouldReceiveBooks(int count) {
        assertEquals(count, lastQueriedBooks.size());
    }

    @When("I request the book by its id")
    public void iRequestTheBookByItsId() {
        lastQueriedBook = bookService.getBookById(lastCreatedBook.id());
    }

    @Then("I should receive the book with title {string}")
    public void iShouldReceiveTheBookWithTitle(String title) {
        assertTrue(lastQueriedBook.isPresent());
        assertEquals(title, lastQueriedBook.get().title());
    }

    @When("I request a book with id {long}")
    public void iRequestABookWithId(long id) {
        lastQueriedBook = bookService.getBookById(id);
    }

    @Then("I should receive a not found response")
    public void iShouldReceiveANotFoundResponse() {
        assertTrue(lastQueriedBook.isEmpty());
    }

    @When("I update the book with title {string}, author {string}, category {string}")
    public void iUpdateTheBook(String title, String author, String category) {
        lastQueriedBook = bookService.updateBook(lastCreatedBook.id(),
            new Book(null, title, author, category, true));
    }

    @When("I update a book with id {long} with title {string}, author {string}, category {string}")
    public void iUpdateABookWithId(long id, String title, String author, String category) {
        lastQueriedBook = bookService.updateBook(id,
            new Book(null, title, author, category, true));
    }

    @Then("the book should have title {string}")
    public void theBookShouldHaveTitle(String title) {
        assertTrue(lastQueriedBook.isPresent());
        assertEquals(title, lastQueriedBook.get().title());
    }

    @And("the book should have author {string}")
    public void theBookShouldHaveAuthor(String author) {
        assertEquals(author, lastQueriedBook.get().author());
    }

    @When("I delete the book")
    public void iDeleteTheBook() {
        try {
            lastDeleteResult = bookService.deleteBook(lastCreatedBook.id());
        } catch (IllegalStateException e) {
            lastErrorMessage = e.getMessage();
            lastDeleteResult = false;
        }
    }

    @When("I delete a book with id {long}")
    public void iDeleteABookWithId(long id) {
        lastDeleteResult = bookService.deleteBook(id);
    }

    @Then("the book should be removed from the library")
    public void theBookShouldBeRemoved() {
        assertTrue(lastDeleteResult);
        assertTrue(bookService.getBookById(lastCreatedBook.id()).isEmpty());
    }

    @Then("the delete should fail")
    public void theDeleteShouldFail() {
        assertFalse(lastDeleteResult);
    }

    @Then("the delete should fail with {string} error")
    public void theDeleteShouldFailWith(String errorMessage) {
        assertFalse(lastDeleteResult);
        assertNotNull(lastErrorMessage);
        assertTrue(lastErrorMessage.contains(errorMessage));
    }
}
