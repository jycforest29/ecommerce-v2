package ecommerce.platform.common.event.product;

import ecommerce.platform.common.event.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

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
    @AllArgsConstructor
    public static class StockInfo {
        private Long productId;
        private Long optionId;
    }
}
