package org.pancakelab.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.pancakelab.exception.IllegalOrderStateException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusTest {

    @ParameterizedTest
    @CsvSource({
            "CREATED, COMPLETED, true",
            "CREATED, CANCELLED, true",
            "CREATED, PREPARED, false",
            "CREATED, DELIVERED, false",
            "COMPLETED, PREPARED, true",
            "COMPLETED, CANCELLED, true",
            "COMPLETED, CREATED, false",
            "COMPLETED, DELIVERED, false",
            "PREPARED, DELIVERED, true",
            "PREPARED, CANCELLED, true",
            "PREPARED, COMPLETED, false",
            "DELIVERED, CANCELLED, false",
            "DELIVERED, PREPARED, false",
            "CANCELLED, CREATED, false",
            "CANCELLED, CANCELLED, false"
    })
    void allowsOnlyLifecycleTransitions(OrderStatus from, OrderStatus to, boolean allowed) {
        assertEquals(allowed, from.canTransitionTo(to));
    }

    @Test
    void onlyCreatedOrdersCanBeEdited() {
        assertTrue(OrderStatus.CREATED.allowsEditing());
        assertFalse(OrderStatus.COMPLETED.allowsEditing());
        assertFalse(OrderStatus.PREPARED.allowsEditing());
        assertFalse(OrderStatus.DELIVERED.allowsEditing());
        assertFalse(OrderStatus.CANCELLED.allowsEditing());
    }

    @Test
    void transitionToReturnsNextStatusWhenAllowed() {
        UUID orderId = UUID.randomUUID();
        assertEquals(OrderStatus.COMPLETED, OrderStatus.CREATED.transitionTo(OrderStatus.COMPLETED, orderId));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.PREPARED.transitionTo(OrderStatus.CANCELLED, orderId));
    }

    @Test
    void transitionToRejectsIllegalMoves() {
        UUID orderId = UUID.randomUUID();
        IllegalOrderStateException exception = assertThrows(
                IllegalOrderStateException.class,
                () -> OrderStatus.CREATED.transitionTo(OrderStatus.PREPARED, orderId));
        assertEquals(
                "Order %s is CREATED; expected PREPARED".formatted(orderId),
                exception.getMessage());
    }
}
