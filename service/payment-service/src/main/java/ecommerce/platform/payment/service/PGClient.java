package ecommerce.platform.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecommerce.platform.payment.controller.PGMockController.PGResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
public class PGClient {

    private static final int MAX_RETRIES = 3;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String pgBaseUrl;

    public PGClient(ObjectMapper objectMapper,
                    @Value("${pg.mock.base-url:http://localhost:8080}") String pgBaseUrl) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        this.objectMapper = objectMapper;
        this.pgBaseUrl = pgBaseUrl;
    }

    public PGResponse requestPayment() {
        return postWithRetry("/pg/mock/payment");
    }

    public PGResponse requestRefund() {
        return postWithRetry("/pg/mock/refund");
    }

    private PGResponse postWithRetry(String path) {
        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return post(path);
            } catch (IOException e) {
                lastException = e;
                log.warn("PG 서버 요청 실패 (시도 {}/{}) - path: {}, error: {}",
                        attempt, MAX_RETRIES, path, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    sleep(attempt * 500L);
                }
            }
        }
        throw new RuntimeException("PG 서버 요청 최종 실패 (" + MAX_RETRIES + "회 재시도)", lastException);
    }

    private PGResponse post(String path) throws IOException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pgBaseUrl + path))
                    .timeout(REQUEST_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), PGResponse.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("PG 서버 요청 중 인터럽트", e);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}