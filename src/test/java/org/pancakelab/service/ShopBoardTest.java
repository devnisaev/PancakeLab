package org.pancakelab.service;

import org.junit.jupiter.api.Test;
import org.pancakelab.api.OrderTicket;
import org.pancakelab.exception.DuplicateIngredientException;
import org.pancakelab.exception.UnknownIngredientException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopBoardTest {
    @Test
    void listsLiveOrdersAsTicketsWithoutDomainTypes() {
        PancakeService shop = new PancakeService();
        UUID first = shop.createOrder(2, 5);
        UUID second = shop.createOrder(1, 1);
        shop.addPancake(second);
        shop.addIngredient(second, "banana");

        List<OrderTicket> tickets = shop.listOrders();

        assertEquals(2, tickets.size());
        assertEquals(second, tickets.get(0).orderId());
        assertEquals(1, tickets.get(0).building());
        assertEquals("CREATED", tickets.get(0).status());
        assertEquals(List.of("Delicious pancake with banana!"), tickets.get(0).pancakes());
        assertEquals(first, tickets.get(1).orderId());
        assertEquals(0, tickets.get(1).pancakeCount());
    }

    @Test
    void addMenuItemMakesANewToppingOrderable() {
        PancakeService shop = new PancakeService();
        shop.addMenuItem("caramel");
        assertTrue(shop.listMenu().contains("caramel"));

        UUID orderId = shop.createOrder(1, 1);
        shop.addPancake(orderId);
        shop.addIngredient(orderId, "caramel");
        assertEquals(List.of("Delicious pancake with caramel!"), shop.viewOrder(orderId));
        assertThrows(DuplicateIngredientException.class, () -> shop.addMenuItem("caramel"));
        assertThrows(UnknownIngredientException.class, () -> shop.addIngredient(orderId, "mustard"));
    }
}
