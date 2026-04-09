package ecommerce.platform.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JsonConverterTest {

    private final JsonConverter jsonConverter = new JsonConverter();

    @Test
    @DisplayName("Map을 JSON 문자열로 변환한다")
    void convertToDatabaseColumn() {
        Map<String, Object> map = Map.of("userId", 1L, "action", "ISSUED");

        String json = jsonConverter.convertToDatabaseColumn(map);

        assertThat(json).startsWith("{");
        assertThat(json).endsWith("}");
        assertThat(json).contains("\"userId\"");
        assertThat(json).contains("\"action\"");
    }

    @Test
    @DisplayName("null Map은 null을 반환한다")
    void convertNullToDatabaseColumn() {
        assertThat(jsonConverter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    @DisplayName("JSON 문자열을 Map으로 변환한다")
    void convertToEntityAttribute() {
        String json = "{\"userId\":1,\"action\":\"ISSUED\"}";

        Map<String, Object> map = jsonConverter.convertToEntityAttribute(json);

        assertThat(map).containsEntry("userId", 1);
        assertThat(map).containsEntry("action", "ISSUED");
    }

    @Test
    @DisplayName("null 문자열은 null을 반환한다")
    void convertNullToEntityAttribute() {
        assertThat(jsonConverter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("빈 Map은 빈 JSON 객체로 변환된다")
    void convertEmptyMap() {
        String json = jsonConverter.convertToDatabaseColumn(Map.of());

        assertThat(json).isEqualTo("{}");

        Map<String, Object> result = jsonConverter.convertToEntityAttribute(json);
        assertThat(result).isEmpty();
    }
}