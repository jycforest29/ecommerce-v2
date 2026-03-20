package ecommerce.platform.user.service;

import ecommerce.platform.user.jwt.util.JwtTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Qualifier("jwtTokenManager")
@Component
@RequiredArgsConstructor
public class JwtTokenManager implements TokenManager {

    private final JwtTokenGenerator jwtTokenGenerator;

    @Override
    public String issueToken(Authentication authentication) {
        return jwtTokenGenerator.createToken(authentication);
    }
}
