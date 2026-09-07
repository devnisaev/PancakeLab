package org.pancakelab.logging;

import org.pancakelab.domain.OrderEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

public final class BoundedShopJournal implements ShopJournal {
    private final ArrayBlockingQueue<OrderEvent> recent;

    public BoundedShopJournal(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Journal capacity must be positive");
        }
        this.recent = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public void record(OrderEvent event) {
        Objects.requireNonNull(event, "event");
        while (!recent.offer(event)) {
            recent.poll();
        }
    }

    public List<OrderEvent> recent() {
        return List.copyOf(new ArrayList<>(recent));
    }
}
