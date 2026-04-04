package clyvasync.Clyvasync.security;

public interface PasswordService {
    boolean isStrongPassword(String password);
    String hashPassword(String password);
    boolean matches(String rawPassword, String encodedPassword);
}
