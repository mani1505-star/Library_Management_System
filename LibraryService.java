import java.util.List;

public class LibraryService {
    private final LibraryRepository repository;

    public LibraryService(LibraryRepository repository) {
        this.repository = repository;
    }

    public Book addBook(String title, String author) {
        validateTitleAndAuthor(title, author);
        return repository.addBook(new Book(0, title.trim(), author.trim()));
    }

    public List<Book> listBooks() {
        return repository.findAllBooks();
    }

    public Book findById(int id) {
        if (id <= 0) throw new IllegalArgumentException("Book ID must be positive");
        return repository.findBookById(id);
    }

    public List<Book> searchByTitle(String title) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title query required");
        return repository.findBooksByTitle(title);
    }

    public void deleteBook(int id) {
        if (id <= 0) throw new IllegalArgumentException("Book ID must be positive");
        repository.deleteBook(id);
    }

    public LibraryResponse issueBook(int bookId, int userId) {
        if (bookId <= 0 || userId <= 0) throw new IllegalArgumentException("IDs must be positive");

        Book book = repository.findBookById(bookId);
        User user = repository.findUserById(userId);

        if (!book.isAvailable()) {
            return new LibraryResponse(false, "Book is already issued");
        }

        book.setAvailable(false);
        book.setIssuedToUserId(user.getId());
        book.setIssueDate(java.time.LocalDate.now());
        repository.updateBook(book);

        return new LibraryResponse(true, "Book issued successfully");
    }

    public LibraryResponse returnBook(int bookId) {
        if (bookId <= 0) throw new IllegalArgumentException("Book ID must be positive");

        Book book = repository.findBookById(bookId);
        if (book.isAvailable()) {
            return new LibraryResponse(false, "Book is not issued");
        }

        FineCalculator.FineResult fine = FineCalculator.calculateFine(book);

        book.setAvailable(true);
        book.setIssuedToUserId(0);
        book.setIssueDate(null);
        repository.updateBook(book);

        return new LibraryResponse(true, String.format("Book returned. Late days: %d, fine: Rs.%d", fine.getLateDays(), fine.getFineAmount()));
    }

    public User addUser(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name required");
        }
        return repository.addUser(new User(0, name.trim()));
    }

    public List<User> listUsers() {
        return repository.findAllUsers();
    }

    public AuthService.LoginResult authenticate(String username, String password) {
        return new AuthService().authenticate(username, password);
    }

    private void validateTitleAndAuthor(String title, String author) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Book title is required");
        if (author == null || author.trim().isEmpty()) throw new IllegalArgumentException("Book author is required");
    }

    public static class LibraryResponse {
        private final boolean success;
        private final String message;

        public LibraryResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
