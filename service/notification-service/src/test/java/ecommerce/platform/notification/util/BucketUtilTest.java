package ecommerce.platform.notification.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BucketUtilTest {

    @Test
    @DisplayName("bucket()은 0 이상 64 미만의 값을 반환한다")
    void bucketRange() {
        for (long userId = 1; userId <= 200; userId++) {
            int bucket = BucketUtil.bucket(userId);
            assertThat(bucket).isBetween(0, BucketUtil.BUCKETS - 1);
        }
    }

    @Test
    @DisplayName("음수 userId도 양수 bucket을 반환한다")
    void negativeBucket() {
        int bucket = BucketUtil.bucket(-5L);
        assertThat(bucket).isBetween(0, BucketUtil.BUCKETS - 1);
    }

    @Test
    @DisplayName("같은 userId는 항상 같은 bucket에 매핑된다")
    void deterministic() {
        int first = BucketUtil.bucket(42L);
        int second = BucketUtil.bucket(42L);
        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("channel()은 notify:{bucket} 형식이다")
    void channelFormat() {
        assertThat(BucketUtil.channel(0)).isEqualTo("notify:0");
        assertThat(BucketUtil.channel(63)).isEqualTo("notify:63");
    }

    @Test
    @DisplayName("channelForUser()는 userId에 맞는 채널을 반환한다")
    void channelForUser() {
        String channel = BucketUtil.channelForUser(42L);
        int expectedBucket = BucketUtil.bucket(42L);
        assertThat(channel).isEqualTo("notify:" + expectedBucket);
    }
}
