package org.pancakelab.domain;

import org.pancakelab.exception.IllegalOrderStateException;

import java.util.UUID;

public enum OrderStatus {
    CREATED,
    COMPLETED,
    PREPARED,
    DELIVERED,
    CANCELLED;

    public boolean allowsEditing() {
        return this == CREATED;
    }

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case CREATED -> next == COMPLETED || next == CANCELLED;
            case COMPLETED -> next == PREPARED || next == CANCELLED;
            case PREPARED -> next == DELIVERED || next == CANCELLED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public OrderStatus transitionTo(OrderStatus next, UUID orderId) {
        if (!canTransitionTo(next)) {
            throw IllegalOrderStateException.unexpected(orderId, this, next);
        }
        return next;
    }
}
