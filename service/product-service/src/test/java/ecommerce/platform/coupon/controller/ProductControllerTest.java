package ecommerce.platform.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecommerce.platform.common.annotations.LoginUserArgumentResolver;
import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.GlobalExceptionHandler;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.coupon.dto.*;
import ecommerce.platform.coupon.service.ProductManageService;
import ecommerce.platform.coupon.service.ProductQueryService;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ProductController productController;

    @Mock
    private ProductManageService productManageService;

    @Mock
    private ProductQueryService productQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @Nested
    @DisplayName("POST /api/v1/products")
    class CreateProduct {

        @Test
        @DisplayName("상품 생성 성공 시 200 반환")
        void createSuccess() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest(
                    "테스트 상품", 1L, Brand.A, Category.OUTER, 50000, "테스트 설명"
            );

            ProductCreateResponse response = new ProductCreateResponse(
                    1L, "테스트 상품", Brand.A, Category.OUTER, 50000, Instant.now()
            );

            given(productManageService.createProduct(eq(1L), any(ProductCreateRequest.class)))
                    .willReturn(response);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.name").value("테스트 상품"))
                    .andExpect(jsonPath("$.brand").value("A"))
                    .andExpect(jsonPath("$.price").value(50000));
        }

        @Test
        @DisplayName("필수값 누락 시 400 반환")
        void createFail_validation() throws Exception {
            String invalidRequest = """
                    {
                        "name": "",
                        "price": 0
                    }
                    """;

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/products/{productId}")
    class ModifyProduct {

        @Test
        @DisplayName("상품 수정 성공 시 200 반환")
        void modifySuccess() throws Exception {
            ProductUpdateRequest request = new ProductUpdateRequest(2L, 60000);

            ProductUpdateResponse response = new ProductUpdateResponse(
                    1L, 2L, 60000, Instant.now()
            );

            given(productManageService.modifyProduct(eq(1L), eq(1L), any(ProductUpdateRequest.class)))
                    .willReturn(response);

            mockMvc.perform(patch("/api/v1/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.imageId").value(2))
                    .andExpect(jsonPath("$.price").value(60000));
        }

        @Test
        @DisplayName("본인 상품이 아니면 403 반환")
        void modifyForbidden() throws Exception {
            ProductUpdateRequest request = new ProductUpdateRequest(2L, 60000);

            given(productManageService.modifyProduct(eq(1L), eq(1L), any(ProductUpdateRequest.class)))
                    .willThrow(new UnauthorizedAccessException());

            mockMvc.perform(patch("/api/v1/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 상품 수정 시 404 반환")
        void modifyNotFound() throws Exception {
            ProductUpdateRequest request = new ProductUpdateRequest(2L, 60000);

            given(productManageService.modifyProduct(eq(999L), eq(1L), any(ProductUpdateRequest.class)))
                    .willThrow(new EntityNotFoundException());

            mockMvc.perform(patch("/api/v1/products/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products")
    class QueryProducts {

        @Test
        @DisplayName("상품 목록 조회 성공")
        void querySuccess() throws Exception {
            List<ProductQueryResponse> responses = List.of(
                    ProductQueryResponse.builder()
                            .productId(1L)
                            .sellerId(1L)
                            .name("상품A")
                            .brand(Brand.A)
                            .category(Category.OUTER)
                            .price(50000)
                            .description("설명A")
                            .reviewCount(3)
                            .inSale(true)
                            .createdAt(Instant.now())
                            .build(),
                    ProductQueryResponse.builder()
                            .productId(2L)
                            .sellerId(2L)
                            .name("상품B")
                            .brand(Brand.B)
                            .category(Category.SHOES)
                            .price(80000)
                            .description("설명B")
                            .reviewCount(0)
                            .inSale(true)
                            .createdAt(Instant.now())
                            .build()
            );

            given(productQueryService.queryProducts()).willReturn(responses);

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].productId").value(1))
                    .andExpect(jsonPath("$[0].name").value("상품A"))
                    .andExpect(jsonPath("$[1].productId").value(2))
                    .andExpect(jsonPath("$[1].name").value("상품B"));
        }

        @Test
        @DisplayName("상품이 없으면 빈 배열 반환")
        void queryEmpty() throws Exception {
            given(productQueryService.queryProducts()).willReturn(List.of());

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{productId}")
    class QueryProduct {

        @Test
        @DisplayName("단건 조회 성공")
        void querySuccess() throws Exception {
            ProductQueryResponse response = ProductQueryResponse.builder()
                    .productId(1L)
                    .sellerId(1L)
                    .name("상품A")
                    .brand(Brand.A)
                    .category(Category.OUTER)
                    .price(50000)
                    .description("설명A")
                    .reviewCount(5)
                    .inSale(true)
                    .createdAt(Instant.now())
                    .build();

            given(productQueryService.queryProduct(1L)).willReturn(response);

            mockMvc.perform(get("/api/v1/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.name").value("상품A"))
                    .andExpect(jsonPath("$.brand").value("A"))
                    .andExpect(jsonPath("$.reviewCount").value(5))
                    .andExpect(jsonPath("$.inSale").value(true));
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 404 반환")
        void queryNotFound() throws Exception {
            given(productQueryService.queryProduct(999L)).willThrow(new EntityNotFoundException());

            mockMvc.perform(get("/api/v1/products/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{productId}")
    class DeleteProduct {

        @Test
        @DisplayName("상품 삭제 성공 시 200 반환")
        void deleteSuccess() throws Exception {
            mockMvc.perform(delete("/api/v1/products/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("본인 상품이 아니면 403 반환")
        void deleteForbidden() throws Exception {
            doThrow(new UnauthorizedAccessException())
                    .when(productManageService).deleteProduct(1L, 1L);

            mockMvc.perform(delete("/api/v1/products/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 상품 삭제 시 404 반환")
        void deleteNotFound() throws Exception {
            doThrow(new EntityNotFoundException())
                    .when(productManageService).deleteProduct(999L, 1L);

            mockMvc.perform(delete("/api/v1/products/999"))
                    .andExpect(status().isNotFound());
        }
    }
}