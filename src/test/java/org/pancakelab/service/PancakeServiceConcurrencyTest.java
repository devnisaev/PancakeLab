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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PancakeServiceConcurrencyTest {
    @Test
    void concurrentOrdersDoNotLosePancakes() throws Exception {
        PancakeService service = new PancakeService();
        int orderCount = 8;
        int pancakesPerOrder = 25;
        ExecutorService pool = Executors.newFixedThreadPool(orderCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<UUID>> results = new ArrayList<>();

        for (int i = 0; i < orderCount; i++) {
            int building = i + 1;
            results.add(pool.submit(() -> {
                start.await();
                UUID orderId = service.createOrder(building, 1);
                for (int p = 0; p < pancakesPerOrder; p++) {
                    service.addPancake(orderId);
                    service.addIngredient(orderId, "dark chocolate");
                }
                return orderId;
            }));
        }

        start.countDown();
        for (Future<UUID> result : results) {
            UUID orderId = result.get(10, TimeUnit.SECONDS);
            assertEquals(pancakesPerOrder, service.viewOrder(orderId).size());
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void concurrentAddsToSameOrderUseStablePancakeIds() throws Exception {
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(1, 1);
        int threads = 10;
        int pancakesEach = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            results.add(pool.submit(() -> {
                start.await();
                for (int i = 0; i < pancakesEach; i++) {
                    int pancakeId = service.addPancake(orderId);
                    service.addIngredient(orderId, pancakeId, "milk chocolate");
                    service.addIngredient(orderId, pancakeId, "hazelnuts");
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> result : results) {
            result.get(10, TimeUnit.SECONDS);
        }

        List<String> pancakes = service.viewOrder(orderId);
        assertEquals(threads * pancakesEach, pancakes.size());
        pancakes.forEach(description ->
                assertEquals("Delicious pancake with milk chocolate, hazelnuts!", description));
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }

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
