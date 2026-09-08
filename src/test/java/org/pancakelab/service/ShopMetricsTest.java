package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.domain.OrderEvent;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShopMetricsTest {
    private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private ShopMetrics metrics;

    @BeforeEach
    void createMetrics() {
        metrics = new ShopMetrics();
    }

    @Test
    void countsLifecycleMilestones() {
        metrics.handle(new OrderEvent.Created(ORDER_ID, 1, 1, 0, 0));
        metrics.handle(new OrderEvent.Completed(ORDER_ID, 1, 1, 1, 1));
        metrics.handle(new OrderEvent.Prepared(ORDER_ID, 1, 1, 1, 2));
        metrics.handle(new OrderEvent.Delivered(ORDER_ID, 1, 1, 1, 3));

        assertEquals(1, metrics.ordersCreated());
        assertEquals(1, metrics.ordersCompleted());
        assertEquals(1, metrics.ordersPrepared());
        assertEquals(1, metrics.ordersDelivered());
        assertEquals(0, metrics.ordersCancelled());
    }

    @Test
    void countsCancelledOrders() {
        metrics.handle(new OrderEvent.Created(ORDER_ID, 1, 1, 0, 0));
        metrics.handle(new OrderEvent.Cancelled(ORDER_ID, 1, 1, 0, 1));

        assertEquals(1, metrics.ordersCreated());
        assertEquals(1, metrics.ordersCancelled());
        assertEquals(0, metrics.ordersDelivered());
    }
}
