import java.util.List;

public interface LibraryRepository {
    Book addBook(Book book) throws RepositoryException;
    List<Book> findAllBooks() throws RepositoryException;
    Book findBookById(int id) throws RepositoryException, NotFoundException;
    List<Book> findBooksByTitle(String title) throws RepositoryException;
    void updateBook(Book book) throws RepositoryException;
    void deleteBook(int id) throws RepositoryException, NotFoundException;

    User addUser(User user) throws RepositoryException;
    List<User> findAllUsers() throws RepositoryException;
    User findUserById(int id) throws RepositoryException, NotFoundException;
    User findUserByName(String name) throws RepositoryException, NotFoundException;
}
