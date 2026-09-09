package org.pancakelab.logging;

import org.pancakelab.domain.OrderEvent;
import org.pancakelab.service.OrderEventListener;

import java.util.List;

public interface ShopJournal extends OrderEventListener {
    void record(OrderEvent event);

    @Override
    default void handle(OrderEvent event) {
        record(event);
    }

    default void recordAll(List<OrderEvent> events) {
        handleAll(events);
    }
}
