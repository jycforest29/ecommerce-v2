package ecommerce.platform.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecommerce.platform.payment.controller.PGMockController.PGResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class PGClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String pgBaseUrl;

    public PGClient(ObjectMapper objectMapper,
                    @Value("${pg.mock.base-url:http://localhost:8080}") String pgBaseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.pgBaseUrl = pgBaseUrl;
    }

    public PGResponse requestPayment() {
        return post("/pg/mock/payment");
    }

    public PGResponse requestRefund() {
        return post("/pg/mock/refund");
    }

    private PGResponse post(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pgBaseUrl + path))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), PGResponse.class);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("PG 서버 요청 실패", e);
        }
    }
}