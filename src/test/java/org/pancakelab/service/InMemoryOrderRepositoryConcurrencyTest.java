package org.pancakelab.service;

import org.junit.jupiter.api.Test;
import org.pancakelab.domain.AddressRegistry;
import org.pancakelab.domain.Order;
import org.pancakelab.domain.OrderRepository;
import org.pancakelab.exception.OrderNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryOrderRepositoryConcurrencyTest {
    @Test
    void sameTicketMutationsSerializeDistinctTicketsDoNotShareALockAndTakeRemovesWithoutNestedLock()
            throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        try {
            sameTicketMutationsSerialize(pool);
            distinctTicketsDoNotShareALock(pool);
            takeRemovesWithoutNestedLock(pool);
        } finally {
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private static void sameTicketMutationsSerialize(ExecutorService pool) throws Exception {
        OrderRepository orders = new InMemoryOrderRepository();
        UUID orderId = saved(orders, 1, 1);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = submit(pool, 10, start, () -> {
            for (int i = 0; i < 10; i++) {
                orders.withOrder(orderId, Order::addPancake);
            }
        });
        start.countDown();
        join(results);
        orders.modify(orderId, saved -> assertEquals(100, saved.pancakeCount()));
    }

    private static void distinctTicketsDoNotShareALock(ExecutorService pool) throws Exception {
        OrderRepository orders = new InMemoryOrderRepository();
        List<UUID> orderIds = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            orderIds.add(saved(orders, i + 1, 1));
        }
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();
        for (UUID orderId : orderIds) {
            results.add(pool.submit(() -> {
                start.await();
                for (int p = 0; p < 25; p++) {
                    orders.withOrder(orderId, Order::addPancake);
                }
                return null;
            }));
        }
        start.countDown();
        join(results);
        for (UUID orderId : orderIds) {
            orders.modify(orderId, saved -> assertEquals(25, saved.pancakeCount()));
        }
    }

    private static void takeRemovesWithoutNestedLock(ExecutorService pool) throws Exception {
        OrderRepository orders = new InMemoryOrderRepository();
        UUID orderId = saved(orders, 10, 20);
        CountDownLatch insideTake = new CountDownLatch(1);
        CountDownLatch finishTake = new CountDownLatch(1);
        Future<?> taking = pool.submit(() -> orders.take(orderId, saved -> {
            insideTake.countDown();
            await(finishTake);
            return saved.getId();
        }));
        assertTrue(insideTake.await(5, TimeUnit.SECONDS));
        Future<?> mutating = pool.submit(() -> {
            assertThrows(OrderNotFoundException.class, () -> orders.withOrder(orderId, Order::addPancake));
            return null;
        });
        finishTake.countDown();
        taking.get(5, TimeUnit.SECONDS);
        mutating.get(5, TimeUnit.SECONDS);
        assertFalse(orders.ids().contains(orderId));
    }

    private static UUID saved(OrderRepository orders, int building, int room) {
        Order order = new Order(AddressRegistry.dojoCampus().require(building, room));
        orders.save(order);
        return order.getId();
    }

    private static List<Future<?>> submit(ExecutorService pool, int threads, CountDownLatch start, Checked task) {
        List<Future<?>> results = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            results.add(pool.submit(() -> {
                start.await();
                task.run();
                return null;
            }));
        }
        return results;
    }

    private static void join(List<Future<?>> results) throws Exception {
        for (Future<?> result : results) {
            result.get(10, TimeUnit.SECONDS);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("timed out");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    @FunctionalInterface
    private interface Checked {
        void run() throws Exception;
    }
}
