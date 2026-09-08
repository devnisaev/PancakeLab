package org.pancakelab.service;

import org.junit.jupiter.api.Test;
import org.pancakelab.domain.AddressRegistry;
import org.pancakelab.domain.IngredientCatalog;
import org.pancakelab.domain.OrderEvent;
import org.pancakelab.exception.UnknownIngredientException;
import org.pancakelab.logging.BoundedShopJournal;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopJournalServiceTest {
    @Test
    void journalsLifecycleOutsideTheOrderLock() {
        BoundedShopJournal journal = new BoundedShopJournal(32);
        PancakeService shop = shop(journal);

        UUID orderId = shop.createOrder(1, 1);
        shop.addPancake(orderId);
        shop.addIngredient(orderId, "dark chocolate");
        shop.completeOrder(orderId);
        shop.prepareOrder(orderId);
        shop.deliverOrder(orderId);

        List<OrderEvent> events = journal.recent();
        assertEquals(OrderEvent.Created.class, events.get(0).getClass());
        assertEquals(OrderEvent.PancakeStarted.class, events.get(1).getClass());
        assertEquals(OrderEvent.IngredientAdded.class, events.get(2).getClass());
        assertEquals(OrderEvent.Completed.class, events.get(3).getClass());
        assertEquals(OrderEvent.Prepared.class, events.get(4).getClass());
        assertEquals(OrderEvent.Delivered.class, events.get(5).getClass());
        assertEquals(orderId, events.get(5).orderId());
        assertEquals(1, events.get(5).pancakeCount());
    }

    @Test
    void doesNotJournalRejectedIngredients() {
        BoundedShopJournal journal = new BoundedShopJournal(8);
        PancakeService shop = shop(journal);
        UUID orderId = shop.createOrder(1, 1);
        shop.addPancake(orderId);
        int before = journal.recent().size();

        assertThrows(UnknownIngredientException.class, () -> shop.addIngredient(orderId, "mustard"));
        assertEquals(before, journal.recent().size());
        assertTrue(shop.viewOrder(orderId).get(0).contains("Delicious pancake!"));
    }

    private static PancakeService shop(BoundedShopJournal journal) {
        return new PancakeService(
                AddressRegistry.dojoCampus(),
                IngredientCatalog.standard(),
                new InMemoryOrderRepository(),
                journal);
    }
}
