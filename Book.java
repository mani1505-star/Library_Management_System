import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Book {
    private int id;
    private String title;
    private String author;
    private boolean available;
    private int issuedToUserId; // 0 if not issued
    private LocalDate issueDate; // null if not issued

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.available = true;
        this.issuedToUserId = 0;
        this.issueDate = null;
    }

    public Book(int id, String title, String author, boolean available, int issuedToUserId, LocalDate issueDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.available = available;
        this.issuedToUserId = issuedToUserId;
        this.issueDate = issueDate;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return available; }
    public int getIssuedToUserId() { return issuedToUserId; }
    public LocalDate getIssueDate() { return issueDate; }

    public void setAvailable(boolean available) { this.available = available; }
    public void setIssuedToUserId(int userId) { this.issuedToUserId = userId; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public String toString() {
        String status = available ? "Available" : String.format("Issued to UID %d on %s", issuedToUserId, issueDate == null ? "N/A" : issueDate.format(DATE_FORMAT));
        return String.format("ID: %d | Title: %s | Author: %s | %s", id, title, author, status);
    }

    public String toCsvRecord() {
        String dateString = issueDate == null ? "" : issueDate.format(DATE_FORMAT);
        return String.format("%d,%s,%s,%b,%d,%s", id, escapeCsv(title), escapeCsv(author), available, issuedToUserId, dateString);
    }

    public static Book fromCsvRecord(String line) throws IllegalArgumentException {
        String[] parts = line.split(",", -1);
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid book CSV format: " + line);
        }
        int id = Integer.parseInt(parts[0]);
        String title = unescapeCsv(parts[1]);
        String author = unescapeCsv(parts[2]);
        boolean available = Boolean.parseBoolean(parts[3]);
        int issuedToUserId = Integer.parseInt(parts[4]);
        LocalDate issueDate = parts[5].isEmpty() ? null : LocalDate.parse(parts[5], DATE_FORMAT);
        return new Book(id, title, author, available, issuedToUserId, issueDate);
    }

    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    private static String unescapeCsv(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        return value;
    }
}
