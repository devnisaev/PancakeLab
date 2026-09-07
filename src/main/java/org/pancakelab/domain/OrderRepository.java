package org.pancakelab.domain;

import org.pancakelab.enums.OrderStatus;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public interface OrderRepository {
    void save(Order order);

    <T> T withOrder(UUID orderId, Function<Order, T> action);

    default void modify(UUID orderId, Consumer<Order> action) {
        withOrder(orderId, order -> {
            action.accept(order);
            return null;
        });
    }

    <T> T take(UUID orderId, Function<Order, T> action);

    void remove(UUID orderId);

    Set<UUID> ids();

    Set<UUID> idsWithStatus(OrderStatus status);
}
