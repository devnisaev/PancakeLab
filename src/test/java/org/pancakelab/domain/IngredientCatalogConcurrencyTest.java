package org.pancakelab.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientCatalogConcurrencyTest {
    @Test
    void concurrentReadsDoNotCorruptTheMenu() throws Exception {
        IngredientCatalog catalog = IngredientCatalog.standard();
        int threads = 12;
        int readsEach = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            results.add(pool.submit(() -> {
                start.await();
                for (int r = 0; r < readsEach; r++) {
                    catalog.require("dark chocolate");
                    catalog.names();
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> result : results) {
            result.get(10, TimeUnit.SECONDS);
        }
        assertEquals(10, catalog.names().size());
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void concurrentAddsRegisterEveryUniqueIngredient() throws Exception {
        IngredientCatalog catalog = new IngredientCatalog(List.of("banana"));
        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger added = new AtomicInteger();
        List<Future<?>> results = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            int suffix = i;
            results.add(pool.submit(() -> {
                start.await();
                try {
                    catalog.add("topping-" + suffix);
                    added.incrementAndGet();
                } catch (RuntimeException ignored) {
                    // duplicate from another thread is fine
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> result : results) {
            result.get(10, TimeUnit.SECONDS);
        }
        assertEquals(threads + 1, catalog.names().size());
        assertTrue(added.get() >= 1);
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }
}
