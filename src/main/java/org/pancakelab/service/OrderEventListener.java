package org.pancakelab.service;

import org.pancakelab.domain.OrderEvent;

import java.util.List;

public interface OrderEventListener {
    OrderEventListener IGNORING = event -> {};

    void handle(OrderEvent event);

    default void handleAll(List<OrderEvent> events) {
        events.forEach(this::handle);
    }
}
