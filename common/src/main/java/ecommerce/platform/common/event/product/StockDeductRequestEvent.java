package ecommerce.platform.common.event.product;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class StockDeductRequestEvent extends Event {
    public static final String TOPIC = "stock.events.deduct_request";
    private Long orderId;
    private List<StockDeductRequestEvent.StockInfo> stockInfos;

    @Builder
    StockDeductRequestEvent(Long orderId, List<StockDeductRequestEvent.StockInfo> stockInfos) {
        super();
        this.orderId = orderId;
        this.stockInfos = stockInfos;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class StockInfo {
        private Long productId;
        private Long optionId;
        private int quantity;
    }
}
