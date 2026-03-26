import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseManager(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    public void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY, name VARCHAR(150) NOT NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS books (id INT PRIMARY KEY, title VARCHAR(255), author VARCHAR(255), available BOOLEAN, issuedToUserId INT, issueDate DATE)");
        }
    }

    public void syncFromLibrary(Library library) throws SQLException {
        // This is a simple demonstration: truncate and repopulate from memory
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM users");
            stmt.executeUpdate("DELETE FROM books");

            try (PreparedStatement userInsert = conn.prepareStatement("INSERT INTO users (id, name) VALUES (?, ?)");
                 PreparedStatement bookInsert = conn.prepareStatement("INSERT INTO books (id, title, author, available, issuedToUserId, issueDate) VALUES (?, ?, ?, ?, ?, ?)");) {

                for (User user : library.listUsers()) {
                    userInsert.setInt(1, user.getId());
                    userInsert.setString(2, user.getName());
                    userInsert.executeUpdate();
                }

                for (Book book : library.listBooks()) {
                    bookInsert.setInt(1, book.getId());
                    bookInsert.setString(2, book.getTitle());
                    bookInsert.setString(3, book.getAuthor());
                    bookInsert.setBoolean(4, book.isAvailable());
                    bookInsert.setInt(5, book.getIssuedToUserId());
                    bookInsert.setDate(6, book.getIssueDate() == null ? null : java.sql.Date.valueOf(book.getIssueDate()));
                    bookInsert.executeUpdate();
                }
            }
        }
    }

    public List<Book> loadBooksFromDb() throws SQLException {
        List<Book> output = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, title, author, available, issuedToUserId, issueDate FROM books");
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                boolean available = rs.getBoolean("available");
                int issuedToUserId = rs.getInt("issuedToUserId");
                java.sql.Date issueDate = rs.getDate("issueDate");
                Book book = new Book(id, title, author, available, issuedToUserId, issueDate == null ? null : issueDate.toLocalDate());
                output.add(book);
            }
        }
        return output;
    }

    public List<User> loadUsersFromDb() throws SQLException {
        List<User> output = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, name FROM users");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                output.add(new User(id, name));
            }
        }
        return output;
    }
}
