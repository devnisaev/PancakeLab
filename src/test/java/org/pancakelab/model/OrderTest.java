package org.pancakelab.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.enums.OrderStatus;
import org.pancakelab.exception.IllegalOrderStateException;
import org.pancakelab.exception.UnknownIngredientException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {
    private final IngredientCatalog catalog = IngredientCatalog.standard();
    private Order order;

    @BeforeEach
    void createOrder() {
        order = new Order(BuildingRegistry.dojoCampus().require(10, 20));
    }

    @Test
    void startsInCreatedStatusAtGivenLocation() {
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(10, order.getBuilding());
        assertEquals(20, order.getRoom());
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
        assertThrows(IllegalArgumentException.class,
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
    void equalsIsBasedOnId() {
        Order other = new Order(BuildingRegistry.dojoCampus().require(1, 1));
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
