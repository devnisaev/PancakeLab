package org.pancakelab.service;

import org.pancakelab.domain.Order;
import org.pancakelab.domain.OrderRepository;
import org.pancakelab.domain.OrderStatus;
import org.pancakelab.exception.OrderNotFoundException;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public final class InMemoryOrderRepository implements OrderRepository {
    private final ConcurrentHashMap<UUID, StoredOrder> orders = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        Objects.requireNonNull(order, "order");
        orders.put(order.getId(), new StoredOrder(order));
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
        StoredOrder stored = orders.get(orderId);
        if (stored == null) {
            return;
        }
        stored.lock.lock();
        try {
            orders.remove(orderId);
        } finally {
            stored.lock.unlock();
        }
    }

    @Override
    public Set<UUID> ids() {
        return Set.copyOf(orders.keySet());
    }

    @Override
    public Set<UUID> idsWithStatus(OrderStatus status) {
        return orders.values().stream()
                .map(stored -> stored.order)
                .filter(order -> order.matchesStatus(status))
                .map(Order::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private <T> T mutate(UUID orderId, Function<Order, T> action, boolean remove) {
        Objects.requireNonNull(orderId, "orderId");
        StoredOrder stored = requireStored(orderId);
        stored.lock.lock();
        try {
            stored = requireStored(orderId);
            T result = action.apply(stored.order);
            if (remove) {
                orders.remove(orderId);
            }
            return result;
        } finally {
            stored.lock.unlock();
        }
    }

    private StoredOrder requireStored(UUID orderId) {
        StoredOrder stored = orders.get(orderId);
        if (stored == null) {
            throw new OrderNotFoundException(orderId);
        }
        return stored;
    }

    private static final class StoredOrder {
        private final Order order;
        private final ReentrantLock lock = new ReentrantLock();

        private StoredOrder(Order order) {
            this.order = Objects.requireNonNull(order, "order");
        }
    }
}
