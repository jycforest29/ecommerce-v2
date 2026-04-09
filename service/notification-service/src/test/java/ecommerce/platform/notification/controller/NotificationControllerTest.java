package ecommerce.platform.notification.controller;

import ecommerce.platform.common.annotations.LoginUserArgumentResolver;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.GlobalExceptionHandler;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.notification.dto.NotificationQueryResponse;
import ecommerce.platform.notification.entity.NotificationType;
import ecommerce.platform.notification.service.NotificationFilter;
import ecommerce.platform.notification.service.NotificationManageService;
import ecommerce.platform.notification.service.NotificationQueryService;
import ecommerce.platform.notification.service.SseEmitterHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private NotificationQueryService notificationQueryService;

    @Mock
    private NotificationManageService notificationManageService;

    @Mock
    private SseEmitterHandler sseEmitterHandler;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @Nested
    @DisplayName("GET /api/v1/notifications")
    class GetNotifications {

        @Test
        @DisplayName("전체 알림 조회 성공")
        void getAll() throws Exception {
            List<NotificationQueryResponse> responses = List.of(
                    NotificationQueryResponse.builder()
                            .notificationId(1L)
                            .notificationType(NotificationType.DELIVERY_STARTED)
                            .title("배송 시작")
                            .body("배송이 시작되었습니다.")
                            .createdAt(Instant.now())
                            .build()
            );

            given(notificationQueryService.getNotifications(1L, NotificationFilter.ALL)).willReturn(responses);

            mockMvc.perform(get("/api/v1/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].notificationId").value(1))
                    .andExpect(jsonPath("$[0].title").value("배송 시작"));
        }

        @Test
        @DisplayName("UNREAD 필터로 조회")
        void getUnread() throws Exception {
            given(notificationQueryService.getNotifications(1L, NotificationFilter.UNREAD)).willReturn(List.of());

            mockMvc.perform(get("/api/v1/notifications")
                            .param("notificationFilter", "UNREAD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notifications/{notificationId}")
    class GetNotification {

        @Test
        @DisplayName("단건 조회 성공")
        void getSuccess() throws Exception {
            NotificationQueryResponse response = NotificationQueryResponse.builder()
                    .notificationId(1L)
                    .notificationType(NotificationType.DELIVERY_COMPLETED)
                    .title("배송 완료")
                    .body("배송이 완료되었습니다.")
                    .createdAt(Instant.now())
                    .build();

            given(notificationQueryService.getNotification(1L, 1L)).willReturn(response);

            mockMvc.perform(get("/api/v1/notifications/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notificationId").value(1))
                    .andExpect(jsonPath("$.title").value("배송 완료"));
        }

        @Test
        @DisplayName("다른 유저의 알림 조회 시 403 반환")
        void getForbidden() throws Exception {
            given(notificationQueryService.getNotification(1L, 1L))
                    .willThrow(new UnauthorizedAccessException());

            mockMvc.perform(get("/api/v1/notifications/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 알림 조회 시 404 반환")
        void getNotFound() throws Exception {
            given(notificationQueryService.getNotification(1L, 999L))
                    .willThrow(new EntityNotFoundException());

            mockMvc.perform(get("/api/v1/notifications/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notifications/stream")
    class Stream {

        @Test
        @DisplayName("SSE 스트림 연결 성공")
        void streamSuccess() throws Exception {
            SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
            given(sseEmitterHandler.addSseEmitter(1L)).willReturn(emitter);

            mockMvc.perform(get("/api/v1/notifications/stream"))
                    .andExpect(status().isOk());

            verify(sseEmitterHandler).addSseEmitter(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/notifications/{notificationId}/read")
    class MarkAsRead {

        @Test
        @DisplayName("읽음 처리 성공 시 200 반환")
        void markAsReadSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/notifications/1/read"))
                    .andExpect(status().isOk());

            verify(notificationManageService).markAsRead(1L, 1L);
        }

        @Test
        @DisplayName("다른 유저의 알림 읽음 처리 시 403 반환")
        void markAsReadForbidden() throws Exception {
            doThrow(new UnauthorizedAccessException()).when(notificationManageService).markAsRead(1L, 1L);

            mockMvc.perform(post("/api/v1/notifications/1/read"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notifications/unread-count")
    class UnreadCount {

        @Test
        @DisplayName("안읽은 알림 수 조회 성공")
        void getUnreadCount() throws Exception {
            given(notificationQueryService.getUnreadCount(1L)).willReturn(5);

            mockMvc.perform(get("/api/v1/notifications/unread-count"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("5"));
        }
    }
}
