package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.exception.IllegalOrderStateException;
import org.pancakelab.exception.InvalidLocationException;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.exception.UnknownIngredientException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PancakeServiceTest {
    private static final String DARK_CHOCOLATE = "Delicious pancake with dark chocolate!";
    private static final String MILK_CHOCOLATE = "Delicious pancake with milk chocolate!";
    private static final String MILK_CHOCOLATE_HAZELNUTS = "Delicious pancake with milk chocolate, hazelnuts!";

    private PancakeService pancakeService;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        pancakeService = new PancakeService();
        orderId = pancakeService.createOrder(10, 20);
    }

    @Test
    void createOrderReturnsIdentifierWithoutExposingDomain() {
        assertThrows(InvalidLocationException.class, () -> pancakeService.createOrder(99, 1));
        assertThrows(OrderNotFoundException.class, () -> pancakeService.viewOrder(UUID.randomUUID()));
    }

    @Test
    void discipleAddsPancakesIngredientByIngredient() {
        addPancakes();

        assertEquals(List.of(
                DARK_CHOCOLATE, DARK_CHOCOLATE, DARK_CHOCOLATE,
                MILK_CHOCOLATE, MILK_CHOCOLATE, MILK_CHOCOLATE,
                MILK_CHOCOLATE_HAZELNUTS, MILK_CHOCOLATE_HAZELNUTS, MILK_CHOCOLATE_HAZELNUTS
        ), pancakeService.viewOrder(orderId));
    }

    @Test
    void discipleRemovesPancakesByDescription() {
        addPancakes();

        pancakeService.removePancakes(DARK_CHOCOLATE, orderId, 2);
        pancakeService.removePancakes(MILK_CHOCOLATE, orderId, 3);
        pancakeService.removePancakes(MILK_CHOCOLATE_HAZELNUTS, orderId, 1);

        assertEquals(List.of(DARK_CHOCOLATE, MILK_CHOCOLATE_HAZELNUTS, MILK_CHOCOLATE_HAZELNUTS),
                pancakeService.viewOrder(orderId));
    }

    @Test
    void completedOrderIsVisibleToChefThenPrepared() {
        addPancakes();
        pancakeService.completeOrder(orderId);

        assertTrue(pancakeService.listCompletedOrders().contains(orderId));

        pancakeService.prepareOrder(orderId);

        assertFalse(pancakeService.listCompletedOrders().contains(orderId));
        assertTrue(pancakeService.listPreparedOrders().contains(orderId));
    }

    @Test
    void deliveringPreparedOrderReturnsSnapshotAndRemovesOrder() {
        addPancakes();
        pancakeService.completeOrder(orderId);
        pancakeService.prepareOrder(orderId);
        List<String> expectedPancakes = pancakeService.viewOrder(orderId);

        DeliveryResult delivered = pancakeService.deliverOrder(orderId);

        assertEquals(orderId, delivered.orderId());
        assertEquals(10, delivered.building());
        assertEquals(20, delivered.room());
        assertEquals(expectedPancakes, delivered.pancakes());
        assertFalse(pancakeService.listPreparedOrders().contains(orderId));
        assertThrows(OrderNotFoundException.class, () -> pancakeService.viewOrder(orderId));
    }

    @Test
    void cancellingOrderRemovesItFromTheDatabase() {
        addPancakes();
        pancakeService.cancelOrder(orderId);

        assertFalse(pancakeService.listCompletedOrders().contains(orderId));
        assertFalse(pancakeService.listPreparedOrders().contains(orderId));
        assertThrows(OrderNotFoundException.class, () -> pancakeService.viewOrder(orderId));
    }

    @Test
    void mustardIsRejected() {
        pancakeService.addPancake(orderId);
        UnknownIngredientException exception = assertThrows(
                UnknownIngredientException.class,
                () -> pancakeService.addIngredient(orderId, "mustard"));
        assertEquals("Unknown ingredient: mustard", exception.getMessage());
        assertEquals(List.of("Delicious pancake!"), pancakeService.viewOrder(orderId));
    }

    @Test
    void cannotPrepareOrderThatIsNotCompleted() {
        addPancakes();
        assertThrows(IllegalOrderStateException.class, () -> pancakeService.prepareOrder(orderId));
    }

    @Test
    void cannotDeliverOrderThatIsNotPrepared() {
        addPancakes();
        pancakeService.completeOrder(orderId);
        assertThrows(IllegalOrderStateException.class, () -> pancakeService.deliverOrder(orderId));
    }

    @Test
    void listCompletedOrdersIsASnapshot() {
        addPancakes();
        pancakeService.completeOrder(orderId);
        Set<UUID> completed = pancakeService.listCompletedOrders();
        assertThrows(UnsupportedOperationException.class, () -> completed.add(UUID.randomUUID()));
    }

    @Test
    void newCombinationsDoNotNeedNewRecipeClasses() {
        pancakeService.addPancake(orderId);
        pancakeService.addIngredient(orderId, "banana");
        pancakeService.addIngredient(orderId, "maple syrup");
        pancakeService.addIngredient(orderId, "whipped cream");

        assertEquals(
                List.of("Delicious pancake with banana, maple syrup, whipped cream!"),
                pancakeService.viewOrder(orderId));
    }

    private void addPancakes() {
        for (int i = 0; i < 3; i++) {
            addPancake("dark chocolate");
        }
        for (int i = 0; i < 3; i++) {
            addPancake("milk chocolate");
        }
        for (int i = 0; i < 3; i++) {
            addPancake("milk chocolate", "hazelnuts");
        }
    }

    private void addPancake(String... ingredients) {
        pancakeService.addPancake(orderId);
        for (String ingredient : ingredients) {
            pancakeService.addIngredient(orderId, ingredient);
        }
    }
}
