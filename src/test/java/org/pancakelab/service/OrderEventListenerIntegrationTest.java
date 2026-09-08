package org.pancakelab.service;

import org.junit.jupiter.api.Test;
import org.pancakelab.domain.AddressRegistry;
import org.pancakelab.domain.IngredientCatalog;
import org.pancakelab.domain.OrderEvent;
import org.pancakelab.logging.BoundedShopJournal;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderEventListenerIntegrationTest {
    @Test
    void serviceNotifiesJournalBoardAndMetricsTogether() {
        BoundedShopJournal journal = new BoundedShopJournal(32);
        KitchenBoard board = new KitchenBoard();
        ShopMetrics metrics = new ShopMetrics();
        PancakeService shop = new PancakeService(
                AddressRegistry.dojoCampus(),
                IngredientCatalog.standard(),
                new InMemoryOrderRepository(),
                new CompositeOrderEventListener(journal, board, metrics));

        UUID orderId = shop.createOrder(3, 12);
        shop.addPancake(orderId);
        shop.addIngredient(orderId, "banana");
        shop.completeOrder(orderId);
        shop.prepareOrder(orderId);
        shop.deliverOrder(orderId);

        List<OrderEvent> events = journal.recent();
        assertEquals(OrderEvent.Delivered.class, events.get(events.size() - 1).getClass());
        assertTrue(board.awaitingPrep().isEmpty());
        assertTrue(board.readyForDelivery().isEmpty());
        assertEquals(1, metrics.ordersCreated());
        assertEquals(1, metrics.ordersCompleted());
        assertEquals(1, metrics.ordersPrepared());
        assertEquals(1, metrics.ordersDelivered());
    }
}
