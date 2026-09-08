package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.domain.AddressRegistry;
import org.pancakelab.domain.Order;
import org.pancakelab.domain.OrderRepository;
import org.pancakelab.enums.OrderStatus;
import org.pancakelab.exception.OrderNotFoundException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryOrderRepositoryTest {
    private OrderRepository orders;
    private Order order;

    @BeforeEach
    void createRepository() {
        orders = new InMemoryOrderRepository();
        order = new Order(AddressRegistry.dojoCampus().require(1, 1));
        orders.save(order);
    }

    @Test
    void withOrderMutatesTheSavedOrder() {
        int pancakeId = orders.withOrder(order.getId(), Order::addPancake);
        assertEquals(0, pancakeId);
        orders.modify(order.getId(), saved ->
                assertEquals(1, saved.pancakeCount()));
    }

    @Test
    void missingOrderIsRejected() {
        UUID missing = UUID.randomUUID();
        assertThrows(OrderNotFoundException.class, () -> orders.withOrder(missing, Order::addPancake));
        assertThrows(NullPointerException.class, () -> orders.withOrder(null, Order::addPancake));
    }

    @Test
    void removeDropsTheOrderFromTheStore() {
        orders.remove(order.getId());
        assertThrows(OrderNotFoundException.class, () -> orders.withOrder(order.getId(), Order::getId));
        assertFalse(orders.idsWithStatus(OrderStatus.CREATED).contains(order.getId()));
    }

    @Test
    void idsListsSavedOrders() {
        assertTrue(orders.ids().contains(order.getId()));
        orders.remove(order.getId());
        assertFalse(orders.ids().contains(order.getId()));
    }

    @Test
    void idsWithStatusReturnsAnUnmodifiableSnapshot() {
        Set<UUID> created = orders.idsWithStatus(OrderStatus.CREATED);
        assertTrue(created.contains(order.getId()));
        assertTrue(orders.idsWithStatus(OrderStatus.COMPLETED).isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> created.add(UUID.randomUUID()));
    }

    @Test
    void takeRemovesTheOrderAfterTheAction() {
        UUID orderId = order.getId();
        UUID removed = orders.take(orderId, Order::getId);
        assertEquals(orderId, removed);
        assertThrows(OrderNotFoundException.class, () -> orders.withOrder(orderId, Order::getId));
    }

    @Test
    void takeLeavesTheOrderWhenTheActionFails() {
        assertThrows(IllegalStateException.class, () ->
                orders.take(order.getId(), saved -> {
                    throw new IllegalStateException("failed");
                }));
        assertTrue(orders.idsWithStatus(OrderStatus.CREATED).contains(order.getId()));
    }
}
