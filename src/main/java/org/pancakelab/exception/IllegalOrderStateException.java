package org.pancakelab.exception;

import java.util.UUID;

public class IllegalOrderStateException extends ShopException {
    public IllegalOrderStateException(String message) {
        super(message);
    }

    public static IllegalOrderStateException unexpected(UUID orderId, Object actual, Object expected) {
        return new IllegalOrderStateException(
                "Order %s is %s; expected %s".formatted(orderId, actual, expected));
    }
}
