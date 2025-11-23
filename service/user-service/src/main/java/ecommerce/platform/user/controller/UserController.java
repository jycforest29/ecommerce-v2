package ecommerce.platform.user.controller;

import ecommerce.platform.user.dto.UserInfoResponse;
import ecommerce.platform.user.dto.UserJoinRequest;
import ecommerce.platform.user.dto.UserLoginRequest;
import ecommerce.platform.user.service.UserAuthenticationService;
import ecommerce.platform.user.service.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserAuthenticationService userAuthenticationService;
    private final UserQueryService userQueryService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@Valid @RequestBody UserJoinRequest userJoinRequest) {
        String token = userAuthenticationService.join(userJoinRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        String token = userAuthenticationService.login(userLoginRequest);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        userAuthenticationService.logout();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw() {
        userAuthenticationService.withdraw();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/details")
    public ResponseEntity<UserInfoResponse> readUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        UserInfoResponse userInfoResponse = userQueryService.getUser(userDetails.getUsername());
        return ResponseEntity.ok(userInfoResponse);
    }
}
