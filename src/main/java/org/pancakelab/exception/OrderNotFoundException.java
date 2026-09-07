package org.pancakelab.exception;

import java.util.UUID;

public class OrderNotFoundException extends ShopException {
    public OrderNotFoundException(UUID orderId) {
        super("Order not found: " + orderId);
    }
}
