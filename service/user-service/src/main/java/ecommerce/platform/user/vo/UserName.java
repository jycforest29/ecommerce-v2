package ecommerce.platform.user.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record UserName (@Column(nullable = false) String userName) {
    private static final String REGEX = "^[A-Za-z0-9]{5,8}$";
    public UserName {
        RegexValidator.validate(userName, REGEX);
    }
}
