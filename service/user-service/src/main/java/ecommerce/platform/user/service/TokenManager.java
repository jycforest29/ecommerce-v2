package ecommerce.platform.user.service;

import org.springframework.security.core.Authentication;

public interface TokenManager {
    String issueToken(Authentication authentication);
}
