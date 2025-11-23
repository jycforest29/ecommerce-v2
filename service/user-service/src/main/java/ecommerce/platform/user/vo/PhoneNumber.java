package ecommerce.platform.user.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PhoneNumber (@Column(nullable = false) String phoneNumber) {
    private static final String REGEX = "^010-\\d{4}-\\d{4}$";
    public PhoneNumber {
        RegexValidator.validate(phoneNumber, REGEX);
    }
}