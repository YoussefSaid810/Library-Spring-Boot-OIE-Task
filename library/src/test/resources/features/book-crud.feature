Feature: Book CRUD Operations
  As a library administrator
  I want to manage books in the system
  So that I can maintain an organized book catalog

  Background:
    Given the library system is running

  ##CREATE

  Scenario: Successfully add a new book
    When I add a book with title "Spring in Action", author "Craig Walls", category "Programming"
    Then the book should be created with a generated id
    And the book should be available

  Scenario: Add multiple books
    When I add a book with title "Book A", author "Author A", category "Fiction"
    And I add a book with title "Book B", author "Author B", category "Science"
    Then the library should have 2 books

  ##READ

  Scenario: Get all books
    Given the library has a book with title "Book 1", author "Author 1", category "Cat 1"
    And the library has a book with title "Book 2", author "Author 2", category "Cat 2"
    When I request all books
    Then I should receive 2 books

  Scenario: Get a book by id
    Given the library has a book with title "Find Me", author "Author", category "Cat"
    When I request the book by its id
    Then I should receive the book with title "Find Me"

  Scenario: Return 404 when book not found
    When I request a book with id 999
    Then I should receive a not found response

  ##UPDATE

  Scenario: Successfully update a book
    Given the library has a book with title "Old Title", author "Old Author", category "Old Cat"
    When I update the book with title "New Title", author "New Author", category "New Cat"
    Then the book should have title "New Title"
    And the book should have author "New Author"

  Scenario: Return 404 when updating non-existent book
    When I update a book with id 999 with title "X", author "Y", category "Z"
    Then I should receive a not found response

  ##DELETE

  Scenario: Successfully delete a book
    Given the library has a book with title "To Delete", author "Author", category "Cat"
    When I delete the book
    Then the book should be removed from the library

  Scenario: Return false when deleting non-existent book
    When I delete a book with id 999
    Then the delete should fail

  Scenario: Prevent deletion of a borrowed book
    Given the library has a book with title "Borrowed Book", author "Author", category "Cat"
    And user 1 has borrowed the book
    When I delete the book
    Then the delete should fail with "currently borrowed" error
