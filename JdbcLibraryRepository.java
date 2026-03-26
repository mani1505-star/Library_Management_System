import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcLibraryRepository implements LibraryRepository {
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public JdbcLibraryRepository(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        initTables();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private void initTables() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(150) NOT NULL UNIQUE)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS books (id INT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(255) NOT NULL, author VARCHAR(255) NOT NULL, available BOOLEAN NOT NULL, issuedToUserId INT, issueDate DATE, CONSTRAINT fk_user FOREIGN KEY (issuedToUserId) REFERENCES users(id) ON DELETE SET NULL)");
        } catch (SQLException e) {
            throw new RepositoryException("Failed to initialize JDBC tables", e);
        }
    }

    @Override
    public Book addBook(Book book) {
        String sql = "INSERT INTO books (title, author, available, issuedToUserId, issueDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setBoolean(3, book.isAvailable());
            stmt.setObject(4, book.getIssuedToUserId() == 0 ? null : book.getIssuedToUserId());
            stmt.setDate(5, book.getIssueDate() == null ? null : Date.valueOf(book.getIssueDate()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    return new Book(generatedId, book.getTitle(), book.getAuthor(), book.isAvailable(), book.getIssuedToUserId(), book.getIssueDate());
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to insert book", e);
        }
        throw new RepositoryException("Failed to retrieve generated book ID");
    }

    @Override
    public List<Book> findAllBooks() {
        String sql = "SELECT id, title, author, available, issuedToUserId, issueDate FROM books";
        List<Book> list = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapBook(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch books", e);
        }
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String author = rs.getString("author");
        boolean available = rs.getBoolean("available");
        int issued = rs.getInt("issuedToUserId");
        Date issueDate = rs.getDate("issueDate");
        return new Book(id, title, author, available, issued, issueDate == null ? null : issueDate.toLocalDate());
    }

    @Override
    public Book findBookById(int id) throws NotFoundException {
        String sql = "SELECT id, title, author, available, issuedToUserId, issueDate FROM books WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapBook(rs);
            }
            throw new NotFoundException("Book not found: " + id);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to query book by id", e);
        }
    }

    @Override
    public List<Book> findBooksByTitle(String title) {
        String sql = "SELECT id, title, author, available, issuedToUserId, issueDate FROM books WHERE LOWER(title) LIKE ?";
        List<Book> list = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + title.toLowerCase() + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapBook(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to query books by title", e);
        }
    }

    @Override
    public void updateBook(Book book) {
        String sql = "UPDATE books SET title=?, author=?, available=?, issuedToUserId=?, issueDate=? WHERE id=?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setBoolean(3, book.isAvailable());
            stmt.setObject(4, book.getIssuedToUserId() == 0 ? null : book.getIssuedToUserId());
            stmt.setDate(5, book.getIssueDate() == null ? null : Date.valueOf(book.getIssueDate()));
            stmt.setInt(6, book.getId());
            int updated = stmt.executeUpdate();
            if (updated == 0) throw new NotFoundException("Book not found: " + book.getId());
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update book", e);
        }
    }

    @Override
    public void deleteBook(int id) throws NotFoundException {
        String sql = "DELETE FROM books WHERE id=?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int removed = stmt.executeUpdate();
            if (removed == 0) throw new NotFoundException("Book not found: " + id);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete book", e);
        }
    }

    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO users (name) VALUES (?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getName());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new User(rs.getInt(1), user.getName());
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to add user", e);
        }
        throw new RepositoryException("Failed to insert user");
    }

    @Override
    public List<User> findAllUsers() {
        String sql = "SELECT id, name FROM users";
        List<User> list = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new User(rs.getInt("id"), rs.getString("name")));
            }
            return list;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch users", e);
        }
    }

    @Override
    public User findUserById(int id) throws NotFoundException {
        String sql = "SELECT id, name FROM users WHERE id=?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(id, rs.getString("name"));
            }
            throw new NotFoundException("User not found: " + id);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to query user by id", e);
        }
    }

    @Override
    public User findUserByName(String name) throws NotFoundException {
        String sql = "SELECT id, name FROM users WHERE LOWER(name)=?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("name"));
            }
            throw new NotFoundException("User not found: " + name);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to query user by name", e);
        }
    }
}
