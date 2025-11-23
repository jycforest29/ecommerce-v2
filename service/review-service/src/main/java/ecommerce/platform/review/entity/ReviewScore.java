package ecommerce.platform.review.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ReviewScore {
    ONE("별로예요", 1),
    TWO("그냥 그래요", 2),
    THREE("보통이에요", 3),
    FOUR("좋아요", 4),
    FIVE("아주 좋아요", 5);

    private final String comment;
    private final int rateScore;

    public static ReviewScore of(int rateScore) {
        return Arrays.stream(values())
                .filter(score -> score.rateScore == rateScore)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 평점입니다: " + rateScore));
    }
}