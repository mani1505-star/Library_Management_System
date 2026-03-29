import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class LibraryGUI extends JFrame {
    private final LibraryService libraryService;
    private final AuthService authService;
    private boolean isAdmin = false;

    private JTextField txtUserName;
    private JPasswordField txtPassword;
    private JLabel lblAuthStatus;
    private JLabel lblStatusBar;

    private JTextField txtBookTitle;
    private JTextField txtBookAuthor;
    private JTextField txtSearchBookId;
    private JTextField txtSearchBookTitle;
    private JTextField txtDeleteBookId;
    private JButton btnAddBook;
    private JButton btnDeleteBook;
    private JButton btnAddUser;

    private JTextField txtUserNameAdd;

    private JTextField txtIssueBookId;
    private JTextField txtIssueUserId;
    private JTextField txtReturnBookId;
    private JLabel lblIssueReturnStatus;

    private JTable bookTable;
    private JTable userTable;

    private DefaultTableModel bookTableModel;
    private DefaultTableModel userTableModel;

    public LibraryGUI() {
        LibraryRepository repository = AppConfig.USE_JDBC
                ? new JdbcLibraryRepository(AppConfig.JDBC_URL, AppConfig.JDBC_USER, AppConfig.JDBC_PASSWORD)
                : new CsvLibraryRepository();
        this.libraryService = new LibraryService(repository);
        this.authService = new AuthService();

        initializeUI();
        setControlsEnabled();
        refreshTables();
        setTitle("Library Management System - Library Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 720);
        setLocationRelativeTo(null);
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Authentication panel
        JPanel authPanel = createAuthPanel();
        panel.add(authPanel, BorderLayout.NORTH);

        // Tabs for operations
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Books", createBooksPanel());
        tabbedPane.add("Users", createUsersPanel());
        tabbedPane.add("Issue / Return", createIssueReturnPanel());
        panel.add(tabbedPane, BorderLayout.CENTER);

        JPanel statusPanel = createStatusBar();
        panel.add(statusPanel, BorderLayout.SOUTH);

        getContentPane().add(panel);
    }

    private JPanel createAuthPanel() {
        JPanel authPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        authPanel.setBorder(BorderFactory.createTitledBorder("Login System"));
        authPanel.add(new JLabel("Username:"));
        txtUserName = new JTextField(10);
        authPanel.add(txtUserName);

        authPanel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField(10);
        authPanel.add(txtPassword);

        JButton btnLogin = new JButton("Login");
        authPanel.add(btnLogin);

        JButton btnLogout = new JButton("Logout");
        authPanel.add(btnLogout);

        lblAuthStatus = new JLabel("Not logged in");
        authPanel.add(lblAuthStatus);

        btnLogin.addActionListener(e -> handleLogin());
        btnLogout.addActionListener(e -> handleLogout());

        return authPanel;
    }

    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        lblStatusBar = new JLabel("Please log in to enable admin actions.");
        lblStatusBar.setForeground(Color.DARK_GRAY);
        statusPanel.add(lblStatusBar, BorderLayout.CENTER);
        return statusPanel;
    }

    private JPanel createBooksPanel() {
        JPanel booksPanel = new JPanel(new BorderLayout(8, 8));

        JPanel controls = new JPanel(new GridLayout(4, 1, 8, 8));
        controls.setBorder(BorderFactory.createTitledBorder("Book Management"));

        // Add book row
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addRow.add(new JLabel("Title:"));
        txtBookTitle = new JTextField(12);
        txtBookTitle.setToolTipText("Enter a book title.");
        addRow.add(txtBookTitle);

        addRow.add(new JLabel("Author:"));
        txtBookAuthor = new JTextField(12);
        txtBookAuthor.setToolTipText("Enter the author's name.");
        addRow.add(txtBookAuthor);

        btnAddBook = new JButton("Add Book");
        btnAddBook.setToolTipText("Add a new book to the library.");
        addRow.add(btnAddBook);
        btnAddBook.addActionListener(e -> addBook());

        controls.add(addRow);

        // Search by ID / title row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchRow.add(new JLabel("Search ID:"));
        txtSearchBookId = new JTextField(5);
        txtSearchBookId.setToolTipText("Search by book ID.");
        searchRow.add(txtSearchBookId);

        JButton btnSearchById = new JButton("Search ID");
        btnSearchById.setToolTipText("Find a book by its ID.");
        searchRow.add(btnSearchById);
        btnSearchById.addActionListener(e -> searchBookById());

        searchRow.add(new JLabel("Search Title:"));
        txtSearchBookTitle = new JTextField(10);
        txtSearchBookTitle.setToolTipText("Enter part or all of a title.");
        searchRow.add(txtSearchBookTitle);

        JButton btnSearchTitle = new JButton("Search Title");
        btnSearchTitle.setToolTipText("Find books whose title contains this text.");
        searchRow.add(btnSearchTitle);
        btnSearchTitle.addActionListener(e -> searchBookByTitle());

        JButton btnClearSearch = new JButton("Clear");
        btnClearSearch.setToolTipText("Clear search filters and show all books.");
        btnClearSearch.addActionListener(e -> {
            clearSearchFields();
            refreshBooks();
        });
        searchRow.add(btnClearSearch);

        controls.add(searchRow);

        // Delete book row
        JPanel deleteRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        deleteRow.add(new JLabel("Delete Book ID:"));
        txtDeleteBookId = new JTextField(5);
        txtDeleteBookId.setToolTipText("Enter the ID of a book to remove.");
        deleteRow.add(txtDeleteBookId);
        btnDeleteBook = new JButton("Delete Book");
        btnDeleteBook.setToolTipText("Remove the selected book from the library.");
        deleteRow.add(btnDeleteBook);
        btnDeleteBook.addActionListener(e -> deleteBook());
        controls.add(deleteRow);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnRefreshBooks = new JButton("Refresh Books");
        btnRefreshBooks.setToolTipText("Reload the book list.");
        btnRefreshBooks.addActionListener(e -> refreshBooks());
        actionRow.add(btnRefreshBooks);
        controls.add(actionRow);

        booksPanel.add(controls, BorderLayout.NORTH);

        String[] bookColumns = {"ID", "Title", "Author", "Availability", "Issued To", "Issue Date"};
        bookTableModel = new DefaultTableModel(bookColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(bookTableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setAutoCreateRowSorter(true);
        bookTable.setRowHeight(24);
        bookTable.setFillsViewportHeight(true);
        bookTable.getTableHeader().setReorderingAllowed(false);
        bookTable.setPreferredScrollableViewportSize(new Dimension(920, 320));
        JScrollPane scrollPane = new JScrollPane(bookTable);
        booksPanel.add(scrollPane, BorderLayout.CENTER);

        return booksPanel;
    }

    private JPanel createUsersPanel() {
        JPanel usersPanel = new JPanel(new BorderLayout(8, 8));

        JPanel addUserRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addUserRow.setBorder(BorderFactory.createTitledBorder("User Management"));

        addUserRow.add(new JLabel("User Name:"));
        txtUserNameAdd = new JTextField(12);
        addUserRow.add(txtUserNameAdd);

        btnAddUser = new JButton("Add User");
        btnAddUser.setToolTipText("Create a new library user.");
        addUserRow.add(btnAddUser);
        btnAddUser.addActionListener(e -> addUser());

        JButton btnRefreshUsers = new JButton("Refresh Users");
        btnRefreshUsers.setToolTipText("Reload the user list.");
        btnRefreshUsers.addActionListener(e -> refreshUsers());
        addUserRow.add(btnRefreshUsers);

        usersPanel.add(addUserRow, BorderLayout.NORTH);

        String[] userColumns = {"ID", "Name"};
        userTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setAutoCreateRowSorter(true);
        userTable.setRowHeight(24);
        userTable.setFillsViewportHeight(true);
        userTable.getTableHeader().setReorderingAllowed(false);
        usersPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        return usersPanel;
    }

    private JPanel createIssueReturnPanel() {
        JPanel issueReturnPanel = new JPanel(new GridLayout(3, 1, 8, 8));

        JPanel issueRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        issueRow.setBorder(BorderFactory.createTitledBorder("Issue Book"));
        issueRow.add(new JLabel("Book ID:"));
        txtIssueBookId = new JTextField(6);
        issueRow.add(txtIssueBookId);
        issueRow.add(new JLabel("User ID:"));
        txtIssueUserId = new JTextField(6);
        issueRow.add(txtIssueUserId);
        JButton btnIssue = new JButton("Issue Book");
        issueRow.add(btnIssue);
        btnIssue.addActionListener(e -> issueBook());

        JPanel returnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        returnRow.setBorder(BorderFactory.createTitledBorder("Return Book"));
        returnRow.add(new JLabel("Book ID:"));
        txtReturnBookId = new JTextField(6);
        returnRow.add(txtReturnBookId);
        JButton btnReturn = new JButton("Return Book");
        returnRow.add(btnReturn);
        btnReturn.addActionListener(e -> returnBook());

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        statusRow.setBorder(BorderFactory.createTitledBorder("Operation Status"));
        lblIssueReturnStatus = new JLabel("Ready.");
        statusRow.add(lblIssueReturnStatus);

        issueReturnPanel.add(issueRow);
        issueReturnPanel.add(returnRow);
        issueReturnPanel.add(statusRow);

        return issueReturnPanel;
    }

    private void handleLogin() {
        String username = txtUserName.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        AuthService.LoginResult result = authService.authenticate(username, password);
        if (result.isSuccess() && result.getRole() == AuthService.Role.ADMIN) {
            isAdmin = true;
            updateAuthState(result.getMessage(), true);
        } else if (result.isSuccess() && result.getRole() == AuthService.Role.GUEST) {
            isAdmin = false;
            updateAuthState(result.getMessage(), true);
        } else {
            isAdmin = false;
            updateAuthState(result.getMessage(), false);
        }

        refreshTables();
    }

    private void handleLogout() {
        isAdmin = false;
        updateAuthState("Logged out", false);
        setControlsEnabled();
    }

    private void updateAuthState(String message, boolean success) {
        lblAuthStatus.setText(message);
        lblAuthStatus.setForeground(success ? new Color(0, 128, 0) : Color.RED);
        lblStatusBar.setText(message + "  |  " + (isAdmin ? "Admin mode enabled" : "Guest mode"));
        lblStatusBar.setForeground(success ? Color.DARK_GRAY : Color.RED.darker());
        setControlsEnabled();
    }

    private void addBook() {
        if (!isAdmin) {
            showError("Only Admin can add books.");
            return;
        }
        String title = txtBookTitle.getText().trim();
        String author = txtBookAuthor.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            showError("Title and author are required.");
            return;
        }

        try {
            Book book = libraryService.addBook(title, author);
            showInfo("Book added: " + book.getTitle());
            clearBookFields();
            refreshBooks();
        } catch (Exception e) {
            showError("Error adding book: " + e.getMessage());
        }
    }

    private void clearBookFields() {
        txtBookTitle.setText("");
        txtBookAuthor.setText("");
    }

    private void addUser() {
        if (!isAdmin) {
            showError("Only Admin can add users.");
            return;
        }
        String name = txtUserNameAdd.getText().trim();
        if (name.isEmpty()) {
            showError("User name is required.");
            return;
        }

        try {
            User user = libraryService.addUser(name);
            showInfo("User added: " + user.getName());
            txtUserNameAdd.setText("");
            refreshUsers();
        } catch (Exception e) {
            showError("Error adding user: " + e.getMessage());
        }
    }

    private void deleteBook() {
        if (!isAdmin) {
            showError("Only Admin can delete books.");
            return;
        }
        try {
            int id = Integer.parseInt(txtDeleteBookId.getText().trim());
            libraryService.deleteBook(id);
            showInfo("Deleted book ID " + id);
            txtDeleteBookId.setText("");
            refreshBooks();
        } catch (NumberFormatException e) {
            showError("Please enter a valid book ID.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void issueBook() {
        try {
            int bookId = Integer.parseInt(txtIssueBookId.getText().trim());
            int userId = Integer.parseInt(txtIssueUserId.getText().trim());
            LibraryService.LibraryResponse response = libraryService.issueBook(bookId, userId);
            if (!response.isSuccess()) {
                showError(response.getMessage());
                return;
            }
            String text = response.getMessage();
            showInfo(text);
            lblIssueReturnStatus.setText(text);
            refreshBooks();
        } catch (NumberFormatException e) {
            showError("Please enter valid numeric IDs for issue.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void returnBook() {
        try {
            int bookId = Integer.parseInt(txtReturnBookId.getText().trim());
            LibraryService.LibraryResponse response = libraryService.returnBook(bookId);
            String text = response.getMessage();
            showInfo(text);
            lblIssueReturnStatus.setText(text);
            refreshBooks();
        } catch (NumberFormatException e) {
            showError("Please enter a valid book ID for return.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void searchBookById() {
        try {
            int bookId = Integer.parseInt(txtSearchBookId.getText().trim());
            Book book = libraryService.findById(bookId);
            bookTableModel.setRowCount(0);
            bookTableModel.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.isAvailable() ? "Available" : "Issued",
                    book.getIssuedToUserId(),
                    book.getIssueDate() == null ? "-" : book.getIssueDate().toString()
            });
            clearSearchFields();
        } catch (NumberFormatException e) {
            showError("Please enter a numeric Book ID for search.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void searchBookByTitle() {
        String title = txtSearchBookTitle.getText().trim();
        if (title.isEmpty()) {
            showError("Please enter a title keyword.");
            return;
        }

        List<Book> results = libraryService.searchByTitle(title);
        if (results.isEmpty()) {
            showInfo("No books found for title: " + title);
            bookTableModel.setRowCount(0);
            return;
        }

        fillBookTable(results);
        clearSearchFields();
    }

    private void clearSearchFields() {
        txtSearchBookId.setText("");
        txtSearchBookTitle.setText("");
    }

    private void refreshBooks() {
        List<Book> books = libraryService.listBooks();
        fillBookTable(books);
    }

    private void refreshUsers() {
        List<User> users = libraryService.listUsers();
        userTableModel.setRowCount(0);
        for (User user : users) {
            userTableModel.addRow(new Object[]{user.getId(), user.getName()});
        }
    }

    private void fillBookTable(List<Book> books) {
        bookTableModel.setRowCount(0);
        for (Book book : books) {
            bookTableModel.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.isAvailable() ? "Available" : "Issued",
                    book.getIssuedToUserId(),
                    book.getIssueDate() == null ? "-" : book.getIssueDate().toString()
            });
        }
    }

    private void refreshTables() {
        refreshBooks();
        refreshUsers();
    }

    private void setControlsEnabled() {
        boolean adminEnabled = isAdmin;
        btnAddBook.setEnabled(adminEnabled);
        btnDeleteBook.setEnabled(adminEnabled);
        btnAddUser.setEnabled(adminEnabled);
        txtBookTitle.setEnabled(adminEnabled);
        txtBookAuthor.setEnabled(adminEnabled);
        txtDeleteBookId.setEnabled(adminEnabled);
        txtUserNameAdd.setEnabled(adminEnabled);
    }

    private void showError(String message) {
        lblStatusBar.setText(message);
        lblStatusBar.setForeground(Color.RED.darker());
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        lblStatusBar.setText(message);
        lblStatusBar.setForeground(new Color(0, 128, 0));
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
