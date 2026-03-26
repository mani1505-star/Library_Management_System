# Library Management System

A Java desktop application demonstrating industry best practices, clean architecture, and modern software design patterns.

## 🎯 Overview

This project is a **complete Library Management System** built with:
- **Core Java** (OOP principles)
- **Layered Architecture** (Model-Service-Repository pattern)
- **Swing GUI** with tabs and tables
- **CSV & JDBC persistence** (CSV by default, MySQL optional)
- **Authentication system** (Admin/Guest roles)
- **Fine calculation** for late book returns
- **Comprehensive error handling** and validation

**Perfect for:** Portfolio, interview prep, learning Java architecture patterns.

---

## ✨ Key Features

### 📚 Book Management
- ✅ Add new books (title, author)
- ✅ View all books in table format
- ✅ Search books by ID or title
- ✅ Delete books (admin-only)
- ✅ Track availability status
- ✅ Monitor issue/return dates

### 👥 User Management
- ✅ Add new users/students
- ✅ View all registered users
- ✅ User lookup by ID/name
- ✅ User role management (future)

### 📤 Issue & Return System
- ✅ Issue books to users
- ✅ Track issue date (stored)
- ✅ Return books with automatic fine calculation
- ✅ Fine: Rs. 5 per day after 14-day limit
- ✅ Display fine amount on return

### 🔐 Authentication & Authorization
- ✅ Admin login (`admin/admin123`)
- ✅ Guest login (`guest/guest`)
- ✅ Role-based UI (admin-only features hidden for guests)
- ✅ Session management

### 💾 Data Persistence
- ✅ CSV file storage (default, no database needed)
- ✅ JDBC MySQL support (optional, configurable)
- ✅ Automatic data load on startup
- ✅ Real-time save on all operations

### ⚙️ Configuration
- ✅ Centralized `AppConfig.java`
- ✅ Toggle CSV vs JDBC mode
- ✅ Customizable fine policy
- ✅ Database credentials in one place

---


### File Structure

```
javProject/
├── AppConfig.java                 # Configuration & constants
├── Book.java                      # Book model with CSV serialization
├── User.java                      # User model with CSV serialization
├── FineCalculator.java            # Fine calculation logic
│
├── LibraryRepository.java         # Repository interface (contract)
├── CsvLibraryRepository.java      # CSV-based persistence
├── JdbcLibraryRepository.java     # MySQL-based persistence
├── RepositoryException.java       # Custom exception
├── NotFoundException.java         # Custom exception
│
├── LibraryService.java            # Core business logic
├── AuthService.java               # Authentication & role management
│
├── LibraryGUI.java                # Swing UI (main view)
├── Main.java                      # Entry point
│
├── DatabaseManager.java           # Utility for DB operations
├── books.csv                      # Auto-generated (CSV mode)
├── users.csv                      # Auto-generated (CSV mode)
└── README.md                      # This file
```

---

## 🛠️ Technology Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 8+ |
| **UI Framework** | Swing (javax.swing) |
| **Data Storage** | CSV (default) / MySQL 5.7+ (optional) |
| **JDBC** | MySQL Connector/J |
| **Design Pattern** | MVC / Layered Architecture |
| **Build** | javac (no build tool required) |

---




### Compile

```bash
javac *.java
```

*All `.class` files generated in same directory.*

### Run

```bash
java Main
```

*GUI window opens automatically.*

---

## 🚀 Quick Start

### 1. **Login**
- Username: `admin`
- Password: `admin123`

### 2. **Add a Book** (Admin only)
- Go to **Books** tab
- Enter Title: `Harry Potter`
- Enter Author: `J.K. Rowling`
- Click **Add Book**

### 3. **Add a User** (Admin only)
- Go to **Users** tab
- Enter User Name: `John Doe`
- Click **Add User**

### 4. **Issue a Book**
- Go to **Issue/Return** tab
- Enter Book ID: `1`
- Enter User ID: `1`
- Click **Issue Book**

### 5. **Return a Book**
- Go to **Issue/Return** tab
- Enter Book ID: `1`
- Click **Return Book**
- ✅ Fine calculated automatically

---

## ⚙️ Configuration

Open `AppConfig.java`:

```java
public class AppConfig {
    // Storage mode
    public static final boolean USE_JDBC = false;  // Set true for MySQL

    // MySQL config (if USE_JDBC=true)
    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/librarydb?useSSL=false&serverTimezone=UTC";
    public static final String JDBC_USER = "root";
    public static final String JDBC_PASSWORD = "password";

    // Fine policy (in Rupees, per day)
    public static final int MAX_BOOK_ISSUE_DAYS = 14;
    public static final int FINE_PER_LATE_DAY = 5;

    // Default credentials
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String GUEST_USERNAME = "guest";
    public static final String GUEST_PASSWORD = "guest";
}
```

### Switch to MySQL Mode

1. **Set `USE_JDBC = true`** in `AppConfig.java`
2. **Create database:**
   ```sql
   CREATE DATABASE librarydb;
   ```
3. **Update JDBC credentials** if different
4. **Recompile and run:**
   ```bash
   javac *.java
   java Main
   ```

Tables auto-create on first run.

---

## 🔐 Authentication & Authorization

| Role | Access |
|------|--------|
| **Admin** | Add/delete books, add users, view all |
| **Guest** | View books, issue/return, view users (read-only) |

### Login Credentials

| Type | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Guest | `guest` | `guest` |

---

## 💰 Fine Calculation System

### Policy
- **Issue Period:** 14 days (default)
- **Fine:** Rs. 5 per day after limit
- **Formula:** `Fine = (Days Used - 14) × 5` (if Days Used > 14)

### Example
- Book issued: 2026-03-26
- Book returned: 2026-04-15 (20 days later)
- Late days: 20 - 14 = 6 days
- Fine: 6 × 5 = **Rs. 30**

---

## 🗄️ Database Setup (MySQL Mode)

### Create Database
```sql
CREATE DATABASE librarydb;
USE librarydb;
```


---

## ❌ Troubleshooting

### Issue: `java.lang.ClassNotFoundException`
**Solution:** Ensure all `.java` files compiled:
```bash
javac *.java
```

### Issue: `NotFoundException: Book not found: 999`
**Solution:** Check book ID exists. Use Search to find valid IDs.

### Issue: `SQLException` when `USE_JDBC=true`
**Solution:**
1. Verify MySQL running: `mysql -u root -p`
2. Database exists: `SHOW DATABASES;` → check `librarydb`
3. Update credentials in `AppConfig.java`
4. Recompile: `javac *.java`

### Issue: `FileNotFoundException: books.csv`
**Solution:** First run creates files automatically. If error persists:
```bash
rm *.csv
javac *.java
java Main
```

---

## 👨‍💻 Author

Your Name  
GitHub: [Mani Singh] (https://github.com/mani1505-star/Library_Management_System.git)
