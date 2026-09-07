package org.pancakelab.service;

import org.pancakelab.domain.Order;
import org.pancakelab.domain.OrderRepository;
import org.pancakelab.enums.OrderStatus;
import org.pancakelab.exception.OrderNotFoundException;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class InMemoryOrderRepository implements OrderRepository {
    private final ConcurrentHashMap<UUID, Order> orders = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        Objects.requireNonNull(order, "order");
        orders.put(order.getId(), order);
    }

    @Override
    public <T> T withOrder(UUID orderId, Function<Order, T> action) {
        return mutate(orderId, action, false);
    }

    @Override
    public <T> T take(UUID orderId, Function<Order, T> action) {
        return mutate(orderId, action, true);
    }

    @Override
    public void remove(UUID orderId) {
        orders.remove(orderId);
    }

    @Override
    public Set<UUID> ids() {
        return Set.copyOf(orders.keySet());
    }

    @Override
    public Set<UUID> idsWithStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(order -> order.matchesStatus(status))
                .map(Order::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private <T> T mutate(UUID orderId, Function<Order, T> action, boolean remove) {
        Objects.requireNonNull(orderId, "orderId");
        AtomicReference<T> result = new AtomicReference<>();
        orders.compute(orderId, (id, order) -> {
            if (order == null) {
                throw new OrderNotFoundException(orderId);
            }
            result.set(action.apply(order));
            return remove ? null : order;
        });
        return result.get();
    }
}
