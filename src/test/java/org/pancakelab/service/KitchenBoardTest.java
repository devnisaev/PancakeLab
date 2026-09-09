package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.domain.OrderEvent;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitchenBoardTest {
    private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private KitchenBoard board;

    @BeforeEach
    void createBoard() {
        board = new KitchenBoard();
    }

    @Test
    void tracksCompletedPreparedDeliveredAndCancelledOrders() {
        board.handle(new OrderEvent.Completed(ORDER_ID, 1, 1, 2, 1));
        assertTrue(board.awaitingPrep().contains(ORDER_ID));
        assertFalse(board.readyForDelivery().contains(ORDER_ID));

        board.handle(new OrderEvent.Prepared(ORDER_ID, 1, 1, 2, 2));
        assertFalse(board.awaitingPrep().contains(ORDER_ID));
        assertTrue(board.readyForDelivery().contains(ORDER_ID));

        board.handle(new OrderEvent.Delivered(ORDER_ID, 1, 1, 2, 3, List.of()));
        assertFalse(board.readyForDelivery().contains(ORDER_ID));
    }

    @Test
    void removesCancelledOrdersFromBothQueues() {
        board.handle(new OrderEvent.Completed(ORDER_ID, 1, 1, 1, 1));
        board.handle(new OrderEvent.Prepared(ORDER_ID, 1, 1, 1, 2));
        board.handle(new OrderEvent.Cancelled(ORDER_ID, 1, 1, 1, 3));

        assertTrue(board.awaitingPrep().isEmpty());
        assertTrue(board.readyForDelivery().isEmpty());
    }

    @Test
    void ignoresEditingEvents() {
        board.handle(new OrderEvent.PancakeStarted(ORDER_ID, 1, 1, 0, 1, 1));
        board.handle(new OrderEvent.IngredientAdded(
                ORDER_ID, 1, 1, 0, "Delicious pancake with banana!", 1, 2));

        assertTrue(board.awaitingPrep().isEmpty());
        assertTrue(board.readyForDelivery().isEmpty());
    }
}
