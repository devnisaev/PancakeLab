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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryOrderRepositoryConcurrencyTest {
    @Test
    void concurrentMutationsOnSameOrderSerializeSafely() throws Exception {
        OrderRepository orders = new InMemoryOrderRepository();
        Order order = new Order(AddressRegistry.dojoCampus().require(1, 1));
        orders.save(order);
        UUID orderId = order.getId();

        int threads = 10;
        int pancakesEach = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            results.add(pool.submit(() -> {
                start.await();
                for (int i = 0; i < pancakesEach; i++) {
                    orders.withOrder(orderId, Order::addPancake);
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> result : results) {
            result.get(10, TimeUnit.SECONDS);
        }

        orders.modify(orderId, saved -> assertEquals(threads * pancakesEach, saved.pancakeCount()));
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void takeBlocksConcurrentMutationsUntilRemoved() throws Exception {
        OrderRepository orders = new InMemoryOrderRepository();
        Order order = new Order(AddressRegistry.dojoCampus().require(2, 2));
        orders.save(order);
        UUID orderId = order.getId();

        CountDownLatch insideTake = new CountDownLatch(1);
        CountDownLatch finishTake = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        Future<?> taking = pool.submit(() -> orders.take(orderId, saved -> {
            insideTake.countDown();
            try {
                finishTake.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            }
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
        assertTrue(orders.ids().isEmpty());
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void parallelOrdersDoNotShareLocks() throws Exception {
        OrderRepository orders = new InMemoryOrderRepository();
        int orderCount = 8;
        ExecutorService pool = Executors.newFixedThreadPool(orderCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger pancakes = new AtomicInteger();
        List<Future<?>> results = new ArrayList<>();

        List<UUID> orderIds = new ArrayList<>();
        for (int i = 0; i < orderCount; i++) {
            Order order = new Order(AddressRegistry.dojoCampus().require(i + 1, 1));
            orders.save(order);
            orderIds.add(order.getId());
        }

        for (UUID orderId : orderIds) {
            results.add(pool.submit(() -> {
                start.await();
                for (int p = 0; p < 25; p++) {
                    orders.withOrder(orderId, Order::addPancake);
                    pancakes.incrementAndGet();
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> result : results) {
            result.get(10, TimeUnit.SECONDS);
        }

        assertEquals(orderCount * 25, pancakes.get());
        for (UUID orderId : orderIds) {
            orders.modify(orderId, saved -> assertEquals(25, saved.pancakeCount()));
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }
}
