package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.exception.IllegalOrderStateException;
import org.pancakelab.exception.InvalidLocationException;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.exception.UnknownIngredientException;
import org.pancakelab.model.BuildingRegistry;
import org.pancakelab.model.IngredientCatalog;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PancakeServiceTddTest {
    private PancakeService shop;

    @BeforeEach
    void createShop() {
        shop = new PancakeService();
    }

    @Test
    void acceptsCampusBoundaryBuildingAndRoom() {
        UUID first = shop.createOrder(1, 1);
        UUID last = shop.createOrder(20, 100);

        shop.addPancake(first);
        shop.addIngredient(first, "blueberry");
        shop.addPancake(last);
        shop.addIngredient(last, "pecan");

        assertEquals(List.of("Delicious pancake with blueberry!"), shop.viewOrder(first));
        assertEquals(List.of("Delicious pancake with pecan!"), shop.viewOrder(last));
    }

    @Test
    void rejectsMissingRoomOnCreate() {
        assertThrows(InvalidLocationException.class, () -> shop.createOrder(10, 0));
        assertThrows(InvalidLocationException.class, () -> shop.createOrder(10, 101));
    }

    @Test
    void ordersDoNotSharePancakes() {
        UUID first = shop.createOrder(1, 1);
        UUID second = shop.createOrder(2, 2);
        addPancake(first, "dark chocolate");
        addPancake(second, "milk chocolate", "hazelnuts");

        assertEquals(List.of("Delicious pancake with dark chocolate!"), shop.viewOrder(first));
        assertEquals(List.of("Delicious pancake with milk chocolate, hazelnuts!"), shop.viewOrder(second));
    }

    @Test
    void cannotAddIngredientBeforeStartingAPancake() {
        UUID orderId = shop.createOrder(1, 1);
        assertThrows(IllegalOrderStateException.class,
                () -> shop.addIngredient(orderId, "dark chocolate"));
    }

    @Test
    void cannotAddIngredientToUnknownPancake() {
        UUID orderId = shop.createOrder(1, 1);
        shop.addPancake(orderId);
        assertThrows(IllegalArgumentException.class,
                () -> shop.addIngredient(orderId, 99, "dark chocolate"));
    }

    @Test
    void addsIngredientToSpecificPancakeWithoutMixing() {
        UUID orderId = shop.createOrder(1, 1);
        int first = shop.addPancake(orderId);
        int second = shop.addPancake(orderId);

        shop.addIngredient(orderId, first, "dark chocolate");
        shop.addIngredient(orderId, second, "banana");
        shop.addIngredient(orderId, first, "hazelnuts");

        assertEquals(List.of(
                "Delicious pancake with dark chocolate, hazelnuts!",
                "Delicious pancake with banana!"
        ), shop.viewOrder(orderId));
    }

    @Test
    void ingredientsAreMatchedIgnoringCase() {
        UUID orderId = shop.createOrder(1, 1);
        shop.addPancake(orderId);
        shop.addIngredient(orderId, "  Dark Chocolate ");

        assertEquals(List.of("Delicious pancake with dark chocolate!"), shop.viewOrder(orderId));
    }

    @Test
    void rejectsMustardEvenOnAChocolatePancake() {
        UUID orderId = shop.createOrder(1, 1);
        shop.addPancake(orderId);
        shop.addIngredient(orderId, "milk chocolate");
        shop.addIngredient(orderId, "whipped cream");

        assertThrows(UnknownIngredientException.class, () -> shop.addIngredient(orderId, "mustard"));
        assertEquals(
                List.of("Delicious pancake with milk chocolate, whipped cream!"),
                shop.viewOrder(orderId));
    }

    @Test
    void cannotCompleteEmptyOrderOrEmptyPancake() {
        UUID empty = shop.createOrder(1, 1);
        assertThrows(IllegalOrderStateException.class, () -> shop.completeOrder(empty));

        UUID unfinished = shop.createOrder(2, 2);
        shop.addPancake(unfinished);
        assertThrows(IllegalOrderStateException.class, () -> shop.completeOrder(unfinished));
    }

    @Test
    void cannotChangePancakesAfterComplete() {
        UUID orderId = completedOrder();

        assertThrows(IllegalOrderStateException.class, () -> shop.addPancake(orderId));
        assertThrows(IllegalOrderStateException.class, () -> shop.addIngredient(orderId, "hazelnuts"));
        assertThrows(IllegalOrderStateException.class,
                () -> shop.removePancakes("Delicious pancake with dark chocolate!", orderId, 1));
    }

    @Test
    void chefCanViewCompletedOrder() {
        UUID orderId = completedOrder();
        assertEquals(List.of("Delicious pancake with dark chocolate!"), shop.viewOrder(orderId));
        assertTrue(shop.listCompletedOrders().contains(orderId));
    }

    @Test
    void cannotCompleteTheSameOrderTwice() {
        UUID orderId = completedOrder();
        assertThrows(IllegalOrderStateException.class, () -> shop.completeOrder(orderId));
    }

    @Test
    void removePancakesRequiresPositiveCount() {
        UUID orderId = shop.createOrder(1, 1);
        addPancake(orderId, "dark chocolate");
        assertThrows(IllegalArgumentException.class,
                () -> shop.removePancakes("Delicious pancake with dark chocolate!", orderId, 0));
    }

    @Test
    void unknownOrderIsRejectedForEveryWorkflowStep() {
        UUID missing = UUID.randomUUID();
        assertThrows(OrderNotFoundException.class, () -> shop.addPancake(missing));
        assertThrows(OrderNotFoundException.class, () -> shop.completeOrder(missing));
        assertThrows(OrderNotFoundException.class, () -> shop.prepareOrder(missing));
        assertThrows(OrderNotFoundException.class, () -> shop.deliverOrder(missing));
        assertThrows(OrderNotFoundException.class, () -> shop.cancelOrder(missing));
    }

    @Test
    void nullOrderIdIsRejected() {
        assertThrows(NullPointerException.class, () -> shop.viewOrder(null));
    }

    @Test
    void cancelRemovesCompletedAndPreparedOrders() {
        UUID completed = completedOrder();
        shop.cancelOrder(completed);
        assertFalse(shop.listCompletedOrders().contains(completed));
        assertThrows(OrderNotFoundException.class, () -> shop.viewOrder(completed));

        UUID prepared = completedOrder();
        shop.prepareOrder(prepared);
        shop.cancelOrder(prepared);
        assertFalse(shop.listPreparedOrders().contains(prepared));
        assertThrows(OrderNotFoundException.class, () -> shop.viewOrder(prepared));
    }

    @Test
    void deliveredPancakesSnapshotCannotBeMutated() {
        UUID orderId = completedOrder();
        shop.prepareOrder(orderId);
        DeliveryResult delivery = shop.deliverOrder(orderId);

        assertThrows(UnsupportedOperationException.class, () -> delivery.pancakes().add("mustard"));
        assertThrows(OrderNotFoundException.class, () -> shop.deliverOrder(orderId));
    }

    @Test
    void listPreparedOrdersIsASnapshot() {
        UUID orderId = completedOrder();
        shop.prepareOrder(orderId);
        Set<UUID> prepared = shop.listPreparedOrders();
        assertThrows(UnsupportedOperationException.class, () -> prepared.add(UUID.randomUUID()));
    }

    @Test
    void customCatalogIsExposedOnlyAsMenuNames() {
        PancakeService limited = new PancakeService(
                BuildingRegistry.dojoCampus(),
                new IngredientCatalog(List.of("banana", "coconut")));

        assertEquals(List.of("banana", "coconut"), limited.listMenu());
        UUID orderId = limited.createOrder(1, 1);
        limited.addPancake(orderId);
        assertThrows(UnknownIngredientException.class, () -> limited.addIngredient(orderId, "dark chocolate"));
        limited.addIngredient(orderId, "banana");
        assertEquals(List.of("Delicious pancake with banana!"), limited.viewOrder(orderId));
    }

    private UUID completedOrder() {
        UUID orderId = shop.createOrder(1, 1);
        addPancake(orderId, "dark chocolate");
        shop.completeOrder(orderId);
        return orderId;
    }

    private void addPancake(UUID orderId, String... ingredients) {
        shop.addPancake(orderId);
        for (String ingredient : ingredients) {
            shop.addIngredient(orderId, ingredient);
        }
    }
}
