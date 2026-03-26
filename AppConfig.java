public class AppConfig {
    // Switch between CSV-file mode and JDBC MySQL persistence
    public static final boolean USE_JDBC = false;

    // MySQL config (use only if USE_JDBC=true)
    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/librarydb?useSSL=false&serverTimezone=UTC";
    public static final String JDBC_USER = "root";
    public static final String JDBC_PASSWORD = "password";

    // Library policy
    public static final int MAX_BOOK_ISSUE_DAYS = 14;
    public static final int FINE_PER_LATE_DAY = 5;

    // Authorization settings
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String GUEST_USERNAME = "guest";
    public static final String GUEST_PASSWORD = "guest";
}
