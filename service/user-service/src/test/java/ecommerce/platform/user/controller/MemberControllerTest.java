package ecommerce.platform.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecommerce.platform.common.annotations.LoginUserArgumentResolver;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.GlobalExceptionHandler;
import ecommerce.platform.user.dto.MemberJoinRequest;
import ecommerce.platform.user.dto.MemberLoginRequest;
import ecommerce.platform.user.dto.MemberQueryResponse;
import ecommerce.platform.user.exception.MemberAlreadyExistsException;
import ecommerce.platform.user.service.UserAuthenticationService;
import ecommerce.platform.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private MemberController memberController;

    @Mock
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private UserQueryService userQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @Nested
    @DisplayName("POST /api/v1/members/join")
    class Join {

        @Test
        @DisplayName("회원가입 성공 시 201과 토큰 반환")
        void joinSuccess() throws Exception {
            MemberJoinRequest request = new MemberJoinRequest(
                    "user01", "Abcde1@x", "test@example.com", "010-1234-5678"
            );
            given(userAuthenticationService.join(any(MemberJoinRequest.class))).willReturn("jwt-token");

            mockMvc.perform(post("/api/v1/members/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().string("jwt-token"));
        }

        @Test
        @DisplayName("이미 존재하는 사용자이면 500 반환")
        void joinDuplicate() throws Exception {
            MemberJoinRequest request = new MemberJoinRequest(
                    "user01", "Abcde1@x", "test@example.com", "010-1234-5678"
            );
            given(userAuthenticationService.join(any(MemberJoinRequest.class)))
                    .willThrow(new MemberAlreadyExistsException());

            mockMvc.perform(post("/api/v1/members/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("필수값 누락 시 400 반환")
        void joinValidationFail() throws Exception {
            String invalidRequest = """
                    {
                        "username": "",
                        "password": ""
                    }
                    """;

            mockMvc.perform(post("/api/v1/members/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/members/login")
    class Login {

        @Test
        @DisplayName("로그인 성공 시 200과 토큰 반환")
        void loginSuccess() throws Exception {
            MemberLoginRequest request = new MemberLoginRequest("user01", "Abcde1@x");
            given(userAuthenticationService.login(any(MemberLoginRequest.class))).willReturn("jwt-token");

            mockMvc.perform(post("/api/v1/members/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("jwt-token"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/members/logout")
    class Logout {

        @Test
        @DisplayName("로그아웃 성공 시 200 반환")
        void logoutSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/members/logout"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/members/withdraw")
    class Withdraw {

        @Test
        @DisplayName("회원탈퇴 성공 시 200 반환")
        void withdrawSuccess() throws Exception {
            mockMvc.perform(delete("/api/v1/members/withdraw"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/details")
    class ReadUserInfo {

        @Test
        @DisplayName("회원정보 조회 성공")
        void readSuccess() throws Exception {
            given(userQueryService.getUser(1L)).willReturn(new MemberQueryResponse("user01"));

            mockMvc.perform(get("/api/v1/members/details"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("user01"));
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 404 반환")
        void readNotFound() throws Exception {
            given(userQueryService.getUser(1L)).willThrow(new EntityNotFoundException());

            mockMvc.perform(get("/api/v1/members/details"))
                    .andExpect(status().isNotFound());
        }
    }
}
