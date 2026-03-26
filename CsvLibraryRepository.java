import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CsvLibraryRepository implements LibraryRepository {
    private static final String BOOKS_FILE = "books.csv";
    private static final String USERS_FILE = "users.csv";

    private final List<Book> books = new ArrayList<>();
    private final List<User> users = new ArrayList<>();

    private int nextBookId = 1;
    private int nextUserId = 1;

    public CsvLibraryRepository() {
        loadUsers();
        loadBooks();
    }

    @Override
    public Book addBook(Book book) {
        book = new Book(nextBookId++, book.getTitle(), book.getAuthor());
        books.add(book);
        saveBooks();
        return book;
    }

    @Override
    public List<Book> findAllBooks() {
        return Collections.unmodifiableList(books);
    }

    @Override
    public Book findBookById(int id) throws NotFoundException {
        return books.stream().filter(b -> b.getId() == id).findFirst().orElseThrow(() -> new NotFoundException("Book not found: " + id));
    }

    @Override
    public List<Book> findBooksByTitle(String title) {
        String q = title.trim().toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(q)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public void updateBook(Book book) {
        int idx = -1;
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId() == book.getId()) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            throw new NotFoundException("Cannot update, book not found: " + book.getId());
        }
        books.set(idx, book);
        saveBooks();
    }

    @Override
    public void deleteBook(int id) throws NotFoundException {
        Book book = findBookById(id);
        books.remove(book);
        saveBooks();
    }

    @Override
    public User addUser(User user) {
        user = new User(nextUserId++, user.getName());
        users.add(user);
        saveUsers();
        return user;
    }

    @Override
    public List<User> findAllUsers() {
        return Collections.unmodifiableList(users);
    }

    @Override
    public User findUserById(int id) throws NotFoundException {
        return users.stream().filter(u -> u.getId() == id).findFirst().orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Override
    public User findUserByName(String name) throws NotFoundException {
        return users.stream().filter(u -> u.getName().equalsIgnoreCase(name)).findFirst().orElseThrow(() -> new NotFoundException("User not found: " + name));
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
        } catch (IOException e) {
            throw new RepositoryException("Error loading books", e);
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
        } catch (IOException e) {
            throw new RepositoryException("Error loading users", e);
        }
    }

    private void saveBooks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (Book book : books) {
                writer.write(book.toCsvRecord());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RepositoryException("Error saving books", e);
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                writer.write(user.toCsvRecord());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RepositoryException("Error saving users", e);
        }
    }
}
