Feature: Book Borrowing Operations
  As a library member
  I want to borrow and return books
  So that I can read books and share them with others

  Background:
    Given the library system is running

  ##BORROW

  Scenario: Successfully borrow an available book
    Given the library has an available book with title "Available Book"
    When user 1 borrows the book
    Then the borrow should succeed
    And the book should no longer be available
    And the borrow record should have status "BORROWED"
    And the due date should be 14 days from today

  Scenario: Prevent borrowing an unavailable book
    Given the library has an available book with title "Taken Book"
    And user 1 has borrowed the book
    When user 2 borrows the book
    Then the borrow should fail with "not available" error

  Scenario: Prevent borrowing when user has 5 active borrows
    Given user 1 has 5 active borrowed books
    And the library has an available book with title "Extra Book"
    When user 1 borrows the book
    Then the borrow should fail with "maximum" error

  Scenario: Prevent borrowing when user has 3 or more overdue books
    Given user 1 has 3 overdue books
    And the library has an available book with title "New Book"
    When user 1 borrows the book
    Then the borrow should fail with "overdue" error

  Scenario: Allow different users to borrow independently
    Given the library has an available book with title "Book A"
    And the library has an available book with title "Book B"
    When user 1 borrows the book with title "Book A"
    And user 2 borrows the book with title "Book B"
    Then both borrows should succeed

  Scenario: Allow borrowing after returning a book
    Given the library has an available book with title "Popular Book"
    And user 1 has borrowed the book
    When user 1 returns the book
    And user 2 borrows the book
    Then the borrow should succeed

  ##RETURN

  Scenario: Successfully return a borrowed book
    Given user 1 has borrowed a book
    When user 1 returns the book
    Then the return should succeed
    And the book should be available again
    And the borrow record should have status "RETURNED"
    And the return date should be set to today

  Scenario: Prevent returning a non-existent record
    When user 1 returns a borrow record with id 999
    Then the return should fail

  Scenario: Prevent double return
    Given user 1 has borrowed a book
    And user 1 has returned the book
    When user 1 returns the book again
    Then the return should fail

  ##BORROW CONSTRAINTS

  Scenario: Overdue detection marks records correctly
    Given user 1 has 2 overdue books
    When I check all borrow records
    Then there should be 2 records with status "OVERDUE"

  Scenario: User can borrow after overdue books are resolved
    Given user 1 has 3 overdue books
    And user 1 has returned all overdue books
    And the library has an available book with title "Retry Book"
    When user 1 borrows the book
    Then the borrow should succeed
