package org.pancakelab.service;

import org.pancakelab.domain.OrderEvent;
import org.pancakelab.domain.OrderEventListener;

import java.util.List;
import java.util.Objects;

public final class CompositeOrderEventListener implements OrderEventListener {
    private final List<OrderEventListener> listeners;

    public CompositeOrderEventListener(List<OrderEventListener> listeners) {
        if (listeners == null || listeners.isEmpty()) {
            throw new IllegalArgumentException("At least one listener is required");
        }
        this.listeners = List.copyOf(listeners);
    }

    @SafeVarargs
    public CompositeOrderEventListener(OrderEventListener... listeners) {
        this(List.of(listeners));
    }

    @Override
    public void handle(OrderEvent event) {
        Objects.requireNonNull(event, "event");
        for (OrderEventListener listener : listeners) {
            listener.handle(event);
        }
    }
}
