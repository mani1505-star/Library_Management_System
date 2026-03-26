public class User {
    private int id;
    private String name;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public String toString() {
        return String.format("User ID: %d | Name: %s", id, name);
    }

    public String toCsvRecord() {
        return String.format("%d,%s", id, escapeCsv(name));
    }

    public static User fromCsvRecord(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid user CSV format: " + line);
        }
        int id = Integer.parseInt(parts[0]);
        String name = unescapeCsv(parts[1]);
        return new User(id, name);
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
