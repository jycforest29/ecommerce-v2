package ecommerce.platform.ranking.controller;

import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.exception.GlobalExceptionHandler;
import ecommerce.platform.ranking.dto.Period;
import ecommerce.platform.ranking.dto.RankingQueryResponse;
import ecommerce.platform.ranking.service.RankingQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RankingControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private RankingController rankingController;

    @Mock
    private RankingQueryService rankingQueryService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rankingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/products/rankings")
    class GetRankingChart {

        @Test
        @DisplayName("카테고리와 기간으로 랭킹을 조회한다")
        void getRankingSuccess() throws Exception {
            List<RankingQueryResponse> responses = List.of(
                    new RankingQueryResponse(1, 3L, "상품C", 300L),
                    new RankingQueryResponse(2, 1L, "상품A", 100L),
                    new RankingQueryResponse(3, 2L, "상품B", 200L)
            );

            given(rankingQueryService.getRanking(Category.OUTER, Period.DAILY)).willReturn(responses);

            mockMvc.perform(get("/api/v1/products/rankings")
                            .param("category", "OUTER")
                            .param("period", "DAILY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].rank").value(1))
                    .andExpect(jsonPath("$[0].productId").value(3))
                    .andExpect(jsonPath("$[0].productName").value("상품C"))
                    .andExpect(jsonPath("$[1].rank").value(2))
                    .andExpect(jsonPath("$[2].rank").value(3));
        }

        @Test
        @DisplayName("랭킹 데이터가 없으면 빈 배열을 반환한다")
        void getRankingEmpty() throws Exception {
            given(rankingQueryService.getRanking(Category.SHOES, Period.REALTIME)).willReturn(List.of());

            mockMvc.perform(get("/api/v1/products/rankings")
                            .param("category", "SHOES")
                            .param("period", "REALTIME"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("필수 파라미터 누락 시 500 반환")
        void missingParams() throws Exception {
            mockMvc.perform(get("/api/v1/products/rankings"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("잘못된 카테고리 값이면 500 반환")
        void invalidCategory() throws Exception {
            mockMvc.perform(get("/api/v1/products/rankings")
                            .param("category", "INVALID")
                            .param("period", "DAILY"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("잘못된 기간 값이면 500 반환")
        void invalidPeriod() throws Exception {
            mockMvc.perform(get("/api/v1/products/rankings")
                            .param("category", "OUTER")
                            .param("period", "INVALID"))
                    .andExpect(status().isInternalServerError());
        }
    }
}
