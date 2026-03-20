package ecommerce.platform.user.controller;

import ecommerce.platform.common.annotations.Login;
import ecommerce.platform.user.dto.MemberJoinRequest;
import ecommerce.platform.user.dto.MemberLoginRequest;
import ecommerce.platform.user.dto.MemberQueryResponse;
import ecommerce.platform.user.service.UserAuthenticationService;
import ecommerce.platform.user.service.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@RestController
public class MemberController {
    private final UserAuthenticationService userAuthenticationService;
    private final UserQueryService userQueryService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@Valid @RequestBody MemberJoinRequest memberJoinRequest) {
        String token = userAuthenticationService.join(memberJoinRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody MemberLoginRequest memberLoginRequest) {
        String token = userAuthenticationService.login(memberLoginRequest);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Login Long userId) {
        userAuthenticationService.logout();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@Login Long userId) {
        userAuthenticationService.withdraw();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/details")
    public ResponseEntity<MemberQueryResponse> readUserInfo(@Login Long userId) {
        MemberQueryResponse response = userQueryService.getUser(userId);
        return ResponseEntity.ok(response);
    }
}
