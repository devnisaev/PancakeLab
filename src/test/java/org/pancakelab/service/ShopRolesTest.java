package org.pancakelab.service;

import org.junit.jupiter.api.Test;
import org.pancakelab.api.DeliveryDesk;
import org.pancakelab.api.DeliveryResult;
import org.pancakelab.api.DiscipleOrders;
import org.pancakelab.api.Kitchen;
import org.pancakelab.api.PancakeShop;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopRolesTest {
    @Test
    void discipleChefAndDeliveryWorkThroughTheirOwnPorts() {
        PancakeShop shop = new PancakeService();
        DiscipleOrders disciple = shop;
        Kitchen kitchen = shop;
        DeliveryDesk delivery = shop;

        UUID orderId = disciple.createOrder(1, 1);
        disciple.addPancake(orderId);
        disciple.addIngredient(orderId, "dark chocolate");
        disciple.completeOrder(orderId);

        assertTrue(kitchen.listCompletedOrders().contains(orderId));
        assertEquals(List.of("Delicious pancake with dark chocolate!"), kitchen.viewOrder(orderId));
        kitchen.prepareOrder(orderId);

        assertTrue(delivery.listPreparedOrders().contains(orderId));
        DeliveryResult result = delivery.deliverOrder(orderId);
        assertEquals(orderId, result.orderId());
        assertEquals(1, result.building());
        assertEquals(1, result.room());
        assertEquals(List.of("Delicious pancake with dark chocolate!"), result.pancakes());
    }
}
