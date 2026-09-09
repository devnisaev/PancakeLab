package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.api.DeliveryResult;
import org.pancakelab.domain.AddressRegistry;
import org.pancakelab.domain.IngredientCatalog;
import org.pancakelab.exception.IllegalOrderStateException;
import org.pancakelab.exception.InvalidLocationException;
import org.pancakelab.exception.InvalidRemovalCountException;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.exception.PancakeNotFoundException;
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
    void rejectsUnknownBuildingAndRoom() {
        assertThrows(InvalidLocationException.class, () -> shop.createOrder(99, 1));
        assertThrows(InvalidLocationException.class, () -> shop.createOrder(10, 0));
        assertThrows(InvalidLocationException.class, () -> shop.createOrder(10, 101));
    }

    @Test
    void ordersDoNotSharePancakes() {
        UUID first = shop.createOrder(1, 1);
        UUID second = shop.createOrder(2, 2);
        addPancake(first, "dark chocolate");
        addPancake(second, "milk chocolate", "hazelnuts");

        assertEquals(List.of(DARK_CHOCOLATE), shop.viewOrder(first));
        assertEquals(List.of(MILK_CHOCOLATE_HAZELNUTS), shop.viewOrder(second));
    }

    @Test
    void discipleAddsPancakesIngredientByIngredient() {
        UUID orderId = orderWithNinePancakes();

        assertEquals(List.of(
                DARK_CHOCOLATE, DARK_CHOCOLATE, DARK_CHOCOLATE,
                MILK_CHOCOLATE, MILK_CHOCOLATE, MILK_CHOCOLATE,
                MILK_CHOCOLATE_HAZELNUTS, MILK_CHOCOLATE_HAZELNUTS, MILK_CHOCOLATE_HAZELNUTS
        ), shop.viewOrder(orderId));
    }

    @Test
    void discipleRemovesPancakesByDescription() {
        UUID orderId = orderWithNinePancakes();

        shop.removePancakes(DARK_CHOCOLATE, orderId, 2);
        shop.removePancakes(MILK_CHOCOLATE, orderId, 3);
        shop.removePancakes(MILK_CHOCOLATE_HAZELNUTS, orderId, 1);

        assertEquals(List.of(DARK_CHOCOLATE, MILK_CHOCOLATE_HAZELNUTS, MILK_CHOCOLATE_HAZELNUTS),
                shop.viewOrder(orderId));
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
        assertThrows(PancakeNotFoundException.class,
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

        assertEquals(List.of(DARK_CHOCOLATE), shop.viewOrder(orderId));
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
    void menuListsAllowedIngredientsAndExcludesMustard() {
        List<String> menu = shop.listMenu();

        assertTrue(menu.contains("dark chocolate"));
        assertTrue(menu.contains("milk chocolate"));
        assertTrue(menu.contains("whipped cream"));
        assertFalse(menu.contains("mustard"));
        assertThrows(UnsupportedOperationException.class, () -> menu.add("mustard"));
    }

    @Test
    void newCombinationsDoNotNeedNewRecipeClasses() {
        UUID orderId = shop.createOrder(10, 20);
        shop.addPancake(orderId);
        shop.addIngredient(orderId, "banana");
        shop.addIngredient(orderId, "maple syrup");
        shop.addIngredient(orderId, "whipped cream");

        assertEquals(
                List.of("Delicious pancake with banana, maple syrup, whipped cream!"),
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
                () -> shop.removePancakes(DARK_CHOCOLATE, orderId, 1));
    }

    @Test
    void cannotCompleteTheSameOrderTwice() {
        UUID orderId = completedOrder();
        assertThrows(IllegalOrderStateException.class, () -> shop.completeOrder(orderId));
    }

    @Test
    void completedOrderIsVisibleToChefThenPrepared() {
        UUID orderId = orderWithNinePancakes();
        shop.completeOrder(orderId);

        assertTrue(shop.listCompletedOrders().contains(orderId));

        shop.prepareOrder(orderId);

        assertFalse(shop.listCompletedOrders().contains(orderId));
        assertTrue(shop.listPreparedOrders().contains(orderId));
    }

    @Test
    void cannotPrepareOrderThatIsNotCompleted() {
        UUID orderId = orderWithNinePancakes();
        assertThrows(IllegalOrderStateException.class, () -> shop.prepareOrder(orderId));
    }

    @Test
    void cannotDeliverOrderThatIsNotPrepared() {
        UUID orderId = completedOrder();
        assertThrows(IllegalOrderStateException.class, () -> shop.deliverOrder(orderId));
    }

    @Test
    void deliveringPreparedOrderReturnsSnapshotAndRemovesOrder() {
        UUID orderId = orderWithNinePancakes();
        shop.completeOrder(orderId);
        shop.prepareOrder(orderId);
        List<String> expectedPancakes = shop.viewOrder(orderId);

        DeliveryResult delivered = shop.deliverOrder(orderId);

        assertEquals(orderId, delivered.orderId());
        assertEquals(10, delivered.building());
        assertEquals(20, delivered.room());
        assertEquals(expectedPancakes, delivered.pancakes());
        assertFalse(shop.listPreparedOrders().contains(orderId));
        assertThrows(OrderNotFoundException.class, () -> shop.viewOrder(orderId));
        assertThrows(UnsupportedOperationException.class, () -> delivered.pancakes().add("mustard"));
        assertThrows(OrderNotFoundException.class, () -> shop.deliverOrder(orderId));
    }

    @Test
    void cancellingOrderRemovesItFromTheDatabase() {
        UUID open = orderWithNinePancakes();
        shop.cancelOrder(open);
        assertThrows(OrderNotFoundException.class, () -> shop.viewOrder(open));

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
    void removePancakesRequiresPositiveCount() {
        UUID orderId = shop.createOrder(1, 1);
        addPancake(orderId, "dark chocolate");
        assertThrows(InvalidRemovalCountException.class,
                () -> shop.removePancakes(DARK_CHOCOLATE, orderId, 0));
    }

    @Test
    void unknownOrderIsRejectedForEveryWorkflowStep() {
        UUID missing = UUID.randomUUID();
        assertThrows(OrderNotFoundException.class, () -> shop.viewOrder(missing));
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
    void listCompletedAndPreparedOrdersAreSnapshots() {
        UUID completed = completedOrder();
        Set<UUID> completedIds = shop.listCompletedOrders();
        assertThrows(UnsupportedOperationException.class, () -> completedIds.add(UUID.randomUUID()));

        shop.prepareOrder(completed);
        Set<UUID> preparedIds = shop.listPreparedOrders();
        assertThrows(UnsupportedOperationException.class, () -> preparedIds.add(UUID.randomUUID()));
    }

    @Test
    void customCatalogIsExposedOnlyAsMenuNames() {
        PancakeService limited = new PancakeService(
                AddressRegistry.dojoCampus(),
                new IngredientCatalog(List.of("banana", "coconut")),
                new InMemoryOrderRepository(),
                OrderEventListener.IGNORING);

        assertEquals(List.of("banana", "coconut"), limited.listMenu());
        UUID orderId = limited.createOrder(1, 1);
        limited.addPancake(orderId);
        assertThrows(UnknownIngredientException.class, () -> limited.addIngredient(orderId, "dark chocolate"));
        limited.addIngredient(orderId, "banana");
        assertEquals(List.of("Delicious pancake with banana!"), limited.viewOrder(orderId));
    }

    private UUID orderWithNinePancakes() {
        UUID orderId = shop.createOrder(10, 20);
        for (int i = 0; i < 3; i++) {
            addPancake(orderId, "dark chocolate");
        }
        for (int i = 0; i < 3; i++) {
            addPancake(orderId, "milk chocolate");
        }
        for (int i = 0; i < 3; i++) {
            addPancake(orderId, "milk chocolate", "hazelnuts");
        }
        return orderId;
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
