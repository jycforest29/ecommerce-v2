package ecommerce.platform.user.service;

public interface PasswordManager {

    String encrypt(String rawPassword);
}
