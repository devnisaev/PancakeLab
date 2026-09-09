package org.pancakelab.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.exception.IllegalOrderStateException;
import org.pancakelab.exception.PancakeNotFoundException;
import org.pancakelab.exception.UnknownIngredientException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {
    private final IngredientCatalog catalog = IngredientCatalog.standard();
    private Order order;

    @BeforeEach
    void createOrder() {
        order = new Order(AddressRegistry.dojoCampus().require(10, 20));
    }

    @Test
    void startsInCreatedStatusAtGivenAddress() {
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(10, order.getBuilding());
        assertEquals(20, order.getRoom());
        assertEquals(0, order.version());
    }

    @Test
    void versionAdvancesOnlyAfterSuccessfulChange() {
        int pancakeId = order.addPancake();
        assertEquals(1, order.version());
        order.addIngredient(pancakeId, catalog.require("dark chocolate"));
        assertEquals(2, order.version());
        assertThrows(IllegalOrderStateException.class, order::prepare);
        assertEquals(2, order.version());
        order.complete();
        assertEquals(3, order.version());
    }

    @Test
    void recordsDomainEventsForSuccessfulChangesOnly() {
        List<OrderEvent> created = order.drainEvents();
        assertEquals(1, created.size());
        assertEquals(new OrderEvent.Created(order.getId(), 10, 20, 0, 0), created.get(0));
        assertTrue(order.drainEvents().isEmpty());

        int pancakeId = order.addPancake();
        order.addIngredient(pancakeId, catalog.require("dark chocolate"));
        List<OrderEvent> changes = order.drainEvents();
        assertEquals(2, changes.size());
        assertTrue(changes.get(0) instanceof OrderEvent.PancakeStarted);
        assertTrue(changes.get(1) instanceof OrderEvent.IngredientAdded);

        assertThrows(IllegalOrderStateException.class, order::prepare);
        assertTrue(order.drainEvents().isEmpty());
    }

    @Test
    void buildsPancakeIngredientByIngredient() {
        int pancakeId = order.addPancake();
        order.addIngredient(pancakeId, catalog.require("milk chocolate"));
        order.addIngredient(pancakeId, catalog.require("hazelnuts"));

        assertEquals(
                "Delicious pancake with milk chocolate, hazelnuts!",
                order.pancakeDescription(pancakeId));
    }

    @Test
    void rejectsIngredientBeforePancakeIsStarted() {
        assertThrows(IllegalOrderStateException.class,
                () -> order.addIngredient(catalog.require("dark chocolate")));
    }

    @Test
    void rejectsMustard() {
        order.addPancake();
        assertThrows(UnknownIngredientException.class, () -> catalog.require("mustard"));
    }

    @Test
    void cannotCompleteEmptyOrder() {
        assertThrows(IllegalOrderStateException.class, order::complete);
    }

    @Test
    void cannotCompleteOrderWithEmptyPancake() {
        order.addPancake();
        assertThrows(IllegalOrderStateException.class, order::complete);
    }

    @Test
    void completePrepareDeliverFollowsLifecycle() {
        order.addPancake();
        order.addIngredient(catalog.require("dark chocolate"));
        order.complete();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());

        order.prepare();
        assertEquals(OrderStatus.PREPARED, order.getStatus());

        order.markDelivered();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void cannotPrepareBeforeComplete() {
        order.addPancake();
        order.addIngredient(catalog.require("dark chocolate"));
        assertThrows(IllegalOrderStateException.class, order::prepare);
    }

    @Test
    void cannotAddIngredientsAfterComplete() {
        order.addPancake();
        order.addIngredient(catalog.require("dark chocolate"));
        order.complete();
        assertThrows(IllegalOrderStateException.class,
                () -> order.addIngredient(catalog.require("hazelnuts")));
    }

    @Test
    void removePancakesKeepsRemainingOnes() {
        addPancakes("dark chocolate", 3);
        int removed = order.removePancakes("Delicious pancake with dark chocolate!", 2);
        assertEquals(2, removed);
        assertEquals(1, order.pancakeCount());
    }

    @Test
    void pancakeIdsStayStableAfterRemoval() {
        int first = order.addPancake();
        order.addIngredient(first, catalog.require("dark chocolate"));
        int second = order.addPancake();
        order.addIngredient(second, catalog.require("milk chocolate"));

        order.removePancakes("Delicious pancake with dark chocolate!", 1);

        order.addIngredient(second, catalog.require("hazelnuts"));
        assertEquals(
                "Delicious pancake with milk chocolate, hazelnuts!",
                order.pancakeDescription(second));
        assertThrows(PancakeNotFoundException.class,
                () -> order.addIngredient(first, catalog.require("hazelnuts")));
    }

    @Test
    void cancelIsAllowedUntilDelivered() {
        order.addPancake();
        order.addIngredient(catalog.require("banana"));
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertThrows(IllegalOrderStateException.class, order::cancel);
    }

    @Test
    void deliveredOrderCannotBeCancelled() {
        order.addPancake();
        order.addIngredient(catalog.require("banana"));
        order.complete();
        order.prepare();
        order.markDelivered();
        assertThrows(IllegalOrderStateException.class, order::cancel);
    }

    @Test
    void equalsIsBasedOnId() {
        Order other = new Order(AddressRegistry.dojoCampus().require(1, 1));
        assertTrue(order.equals(order));
        assertEquals(order.hashCode(), order.hashCode());
        assertEquals(false, order.equals(other));
    }

    private void addPancakes(String ingredient, int count) {
        for (int i = 0; i < count; i++) {
            order.addPancake();
            order.addIngredient(catalog.require(ingredient));
        }
    }
}
