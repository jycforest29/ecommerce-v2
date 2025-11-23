package ecommerce.platform.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Qualifier("bCryptPasswordManager")
@Component
@RequiredArgsConstructor
public class BCryptPasswordManager implements PasswordManager {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public String encrypt(String rawPassword) {
        return bCryptPasswordEncoder.encode(rawPassword);
    }
}
