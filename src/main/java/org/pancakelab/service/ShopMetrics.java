package org.pancakelab.service;

import org.pancakelab.domain.OrderEvent;

import java.util.concurrent.atomic.LongAdder;

public final class ShopMetrics implements OrderEventListener {
    private final LongAdder ordersCreated = new LongAdder();
    private final LongAdder ordersCompleted = new LongAdder();
    private final LongAdder ordersPrepared = new LongAdder();
    private final LongAdder ordersDelivered = new LongAdder();
    private final LongAdder ordersCancelled = new LongAdder();

    @Override
    public void handle(OrderEvent event) {
        if (event instanceof OrderEvent.Created) {
            ordersCreated.increment();
        } else if (event instanceof OrderEvent.Completed) {
            ordersCompleted.increment();
        } else if (event instanceof OrderEvent.Prepared) {
            ordersPrepared.increment();
        } else if (event instanceof OrderEvent.Delivered) {
            ordersDelivered.increment();
        } else if (event instanceof OrderEvent.Cancelled) {
            ordersCancelled.increment();
        }
    }

    public long ordersCreated() {
        return ordersCreated.sum();
    }

    public long ordersCompleted() {
        return ordersCompleted.sum();
    }

    public long ordersPrepared() {
        return ordersPrepared.sum();
    }

    public long ordersDelivered() {
        return ordersDelivered.sum();
    }

    public long ordersCancelled() {
        return ordersCancelled.sum();
    }
}
