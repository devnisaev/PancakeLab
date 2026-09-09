package org.pancakelab.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PancakeServiceConcurrencyTest {
    @Test
    void listingCompletedOrdersDoesNotBlockOrLoseUpdates() throws Exception {
        PancakeService service = new PancakeService();
        int orderCount = 20;
        ExecutorService pool = Executors.newFixedThreadPool(orderCount + 2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<UUID>> created = new ArrayList<>();

        for (int i = 0; i < orderCount; i++) {
            int building = i + 1;
            created.add(pool.submit(() -> {
                start.await();
                UUID orderId = service.createOrder(building, 1);
                service.addPancake(orderId);
                service.addIngredient(orderId, "banana");
                service.completeOrder(orderId);
                return orderId;
            }));
        }
        Future<?> listing = pool.submit(() -> {
            start.await();
            for (int i = 0; i < 200; i++) {
                service.listCompletedOrders();
            }
            return null;
        });

        start.countDown();
        listing.get(10, TimeUnit.SECONDS);
        for (Future<UUID> result : created) {
            UUID orderId = result.get(10, TimeUnit.SECONDS);
            assertTrue(service.listCompletedOrders().contains(orderId));
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }
}
