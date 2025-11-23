package ecommerce.platform.notification.util;

public final class BucketUtil {
    public static final int BUCKETS = 64;

    public static int bucket(Long userId) {
        long mod = userId % BUCKETS;
        return (int) (mod < 0 ? mod + BUCKETS : mod);
    }

    public static String channel(int i) {
        return "notify:" + i;
    }

    public static String channelForUser(Long userId) {
        return channel(bucket(userId));
    }
}
