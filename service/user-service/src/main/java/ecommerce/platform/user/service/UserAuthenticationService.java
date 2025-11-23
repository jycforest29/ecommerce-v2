package ecommerce.platform.user.service;

import ecommerce.platform.user.dto.UserJoinRequest;
import ecommerce.platform.user.dto.UserLoginRequest;
import ecommerce.platform.user.exception.UserAlreadyExistsException;
import ecommerce.platform.user.entity.UserEntity;
import ecommerce.platform.user.repository.LogoutTokenRepository;
import ecommerce.platform.user.repository.UserRepository;
import ecommerce.platform.user.vo.Email;
import ecommerce.platform.user.vo.Password;
import ecommerce.platform.user.vo.PhoneNumber;
import ecommerce.platform.user.vo.UserName;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthenticationService {

    private final UserRepository userRepository;

    private final LogoutTokenRepository logoutTokenRepository;

    private final AuthenticationManager authenticationManager;

    @Qualifier("jwtTokenManager")
    private final TokenManager tokenManager;

    @Qualifier("bCryptPasswordManager")
    private final PasswordManager passwordManager;

    @Transactional
    public String join(UserJoinRequest userJoinRequest) {
        String username = userJoinRequest.username();
        String password = userJoinRequest.password();

        if (!userRepository.existsByUserName(username)) {
            Password.validateRawPassword(password);
            String encryptedPassword = passwordManager.encrypt(password);
            UserEntity userEntity = UserEntity.builder()
                            .userName(new UserName(username))
                            .password(new Password(encryptedPassword))
                            .email(new Email(userJoinRequest.email()))
                            .phoneNumber(new PhoneNumber(userJoinRequest.phoneNumber()))
                            .build();

            userRepository.saveAndFlush(userEntity);

            return login(username, password);
        }
        throw new UserAlreadyExistsException();
    }

    @Transactional
    public String login(UserLoginRequest userLoginRequest) {
        return login(userLoginRequest.userName(), userLoginRequest.password());
    }

    private String login(String userName, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenManager.issueToken(authentication);
        return token;
    }

    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = (String) authentication.getCredentials();
        SecurityContextHolder.clearContext();
        logoutTokenRepository.add(token);
    }

    @Transactional
    public void withdraw() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        userRepository.deleteByUserName(user.getUsername());
        logout();
    }
}
