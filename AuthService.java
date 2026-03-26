public class AuthService {
    public enum Role {
        ADMIN,
        GUEST,
        INVALID
    }

    public static class LoginResult {
        private final Role role;
        private final String message;

        public LoginResult(Role role, String message) {
            this.role = role;
            this.message = message;
        }

        public Role getRole() {
            return role;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return role != Role.INVALID;
        }
    }

    public LoginResult authenticate(String username, String password) {
        if (username == null || password == null) {
            return new LoginResult(Role.INVALID, "Username and password required");
        }

        username = username.trim();
        password = password.trim();

        if (username.equals(AppConfig.ADMIN_USERNAME) && password.equals(AppConfig.ADMIN_PASSWORD)) {
            return new LoginResult(Role.ADMIN, "Admin authenticated");
        }

        if (username.equals(AppConfig.GUEST_USERNAME) && password.equals(AppConfig.GUEST_PASSWORD)) {
            return new LoginResult(Role.GUEST, "Guest authenticated");
        }

        return new LoginResult(Role.INVALID, "Invalid credentials");
    }

    public LoginResult loginUser(User user) {
        if (user == null) {
            return new LoginResult(Role.INVALID, "User not found");
        }
        return new LoginResult(Role.GUEST, "User authenticated: " + user.getName());
    }
}
