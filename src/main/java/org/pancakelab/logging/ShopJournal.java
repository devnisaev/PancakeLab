package org.pancakelab.logging;

import org.pancakelab.domain.OrderEvent;

import java.util.List;

public interface ShopJournal {
    void record(OrderEvent event);

    default void recordAll(List<OrderEvent> events) {
        events.forEach(this::record);
    }
}
