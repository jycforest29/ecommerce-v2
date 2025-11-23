package ecommerce.platform.notification.util;

import java.util.UUID;

public class ConnectionIdGenerator {
    public static String generateId() {
        return UUID.randomUUID().toString();
    }
}
