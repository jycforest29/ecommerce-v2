package ecommerce.platform.user.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Email(@Column(nullable = false) String email) {
    private static final String REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public Email {
        RegexValidator.validate(email, REGEX);
    }
}
