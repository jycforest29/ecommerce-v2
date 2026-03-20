package ecommerce.platform.common.event.product;

import ecommerce.platform.common.event.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class StockRestoreRequestEvent extends Event {
    public static final String TOPIC = "stock.events.restore_request";
    private Long orderId;
    private List<StockDeductRequestEvent.StockInfo> stockInfos;

    protected StockRestoreRequestEvent() {}

    @Builder
    StockRestoreRequestEvent(Long orderId, List<StockDeductRequestEvent.StockInfo> stockInfos) {
        super();
        this.orderId = orderId;
        this.stockInfos = stockInfos;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
