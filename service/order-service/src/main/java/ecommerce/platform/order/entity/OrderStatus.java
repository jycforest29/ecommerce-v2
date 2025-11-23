package ecommerce.platform.order.entity;

public enum OrderStatus {
    CREATED,
    COUPON_APPLIED,
    STOCK_DEDUCTED,
    PAYMENT_PENDING,
    PAID,
    CANCELLED
}
