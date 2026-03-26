import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Library {
    private final List<Book> books = new ArrayList<>();
    private final List<User> users = new ArrayList<>();

    private int nextBookId = 1;
    private int nextUserId = 1;

    private static final String BOOKS_FILE = "books.csv";
    private static final String USERS_FILE = "users.csv";

    public Library() {
        loadFromFiles();
    }

    // ---------- Book management ----------

    public Book addBook(String title, String author) {
        Book book = new Book(nextBookId++, title.trim(), author.trim());
        books.add(book);
        saveToFiles();
        return book;
    }

    public List<Book> listBooks() {
        return new ArrayList<>(books);
    }

    public Book findBookById(int id) throws NotFoundException {
        return books.stream().filter(b -> b.getId() == id).findFirst().orElseThrow(() -> new NotFoundException("Book id " + id + " not found."));
    }

    public List<Book> findBooksByTitle(String title) {
        String down = title.trim().toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(down)) {
                result.add(book);
            }
        }
        return result;
    }

    public void deleteBook(int id) throws NotFoundException {
        Book book = findBookById(id);
        books.remove(book);
        saveToFiles();
    }

    public FineResult issueBook(int bookId, int userId) throws NotFoundException, IllegalStateException {
        Book book = findBookById(bookId);
        User user = findUserById(userId);

        if (!book.isAvailable()) {
            throw new IllegalStateException("Book is already issued.");
        }

        book.setAvailable(false);
        book.setIssuedToUserId(user.getId());
        book.setIssueDate(LocalDate.now());
        saveToFiles();
        return new FineResult(0, 0); // no fine at issue
    }

    public FineResult returnBook(int bookId) throws NotFoundException, IllegalStateException {
        Book book = findBookById(bookId);
        if (book.isAvailable()) {
            throw new IllegalStateException("Book is not issued.");
        }
        LocalDate issueDate = book.getIssueDate();
        if (issueDate == null) {
            throw new IllegalStateException("Issue date missing.");
        }

        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(issueDate, today);
        long allowed = 14;
        long finePerDay = 5;
        long lateDays = Math.max(0, days - allowed);
        long totalFine = lateDays * finePerDay;

        book.setAvailable(true);
        book.setIssuedToUserId(0);
        book.setIssueDate(null);
        saveToFiles();

        return new FineResult(lateDays, totalFine);
    }

    // ---------- User management ----------

    public User addUser(String name) {
        User user = new User(nextUserId++, name.trim());
        users.add(user);
        saveToFiles();
        return user;
    }

    public List<User> listUsers() {
        return new ArrayList<>(users);
    }

    public User findUserById(int id) throws NotFoundException {
        return users.stream().filter(u -> u.getId() == id).findFirst().orElseThrow(() -> new NotFoundException("User id " + id + " not found."));
    }

    public static class FineResult {
        private final long lateDays;
        private final long fineAmount;

        public FineResult(long lateDays, long fineAmount) {
            this.lateDays = lateDays;
            this.fineAmount = fineAmount;
        }

        public long getLateDays() { return lateDays; }
        public long getFineAmount() { return fineAmount; }
    }

    // ---------- Persistence ----------

    private void loadFromFiles() {
        loadUsers();
        loadBooks();
    }

    private void loadBooks() {
        File file = new File(BOOKS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int maxId = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Book book = Book.fromCsvRecord(line);
                books.add(book);
                maxId = Math.max(maxId, book.getId());
            }
            nextBookId = Math.max(nextBookId, maxId + 1);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading books: " + e.getMessage());
        }
    }

    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int maxId = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                User user = User.fromCsvRecord(line);
                users.add(user);
                maxId = Math.max(maxId, user.getId());
            }
            nextUserId = Math.max(nextUserId, maxId + 1);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void saveToFiles() {
        saveBooks();
        saveUsers();
    }

    private void saveBooks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (Book book : books) {
                writer.write(book.toCsvRecord());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving books: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                writer.write(user.toCsvRecord());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}
