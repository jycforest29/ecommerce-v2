package ecommerce.platform.user.service;

import ecommerce.platform.user.dto.MemberJoinRequest;
import ecommerce.platform.user.dto.MemberLoginRequest;
import ecommerce.platform.user.exception.MemberAlreadyExistsException;
import ecommerce.platform.user.entity.Member;
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
    public String join(MemberJoinRequest memberJoinRequest) {
        String username = memberJoinRequest.username();
        String password = memberJoinRequest.password();

        if (!userRepository.existsByUserName(username)) {
            Password.validateRawPassword(password);
            String encryptedPassword = passwordManager.encrypt(password);
            Member member = Member.builder()
                            .userName(new UserName(username))
                            .password(new Password(encryptedPassword))
                            .email(new Email(memberJoinRequest.email()))
                            .phoneNumber(new PhoneNumber(memberJoinRequest.phoneNumber()))
                            .build();

            userRepository.saveAndFlush(member);

            return login(username, password);
        }
        throw new MemberAlreadyExistsException();
    }

    public String login(MemberLoginRequest memberLoginRequest) {
        return login(memberLoginRequest.userName(), memberLoginRequest.password());
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
