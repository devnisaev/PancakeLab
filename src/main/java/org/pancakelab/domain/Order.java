package org.pancakelab.domain;

import org.pancakelab.enums.OrderStatus;
import org.pancakelab.exception.IllegalOrderStateException;
import org.pancakelab.exception.InvalidRemovalCountException;
import org.pancakelab.exception.PancakeNotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Order {
    private final UUID id;
    private final Address address;
    private final List<Pancake> pancakes = new ArrayList<>();
    private volatile OrderStatus status = OrderStatus.CREATED;
    private volatile int version;
    private int nextPancakeId = 0;
    private final List<OrderEvent> events = new ArrayList<>();

    public Order(Address address) {
        this(UUID.randomUUID(), address);
    }

    Order(UUID id, Address address) {
        this.id = Objects.requireNonNull(id, "id");
        this.address = Objects.requireNonNull(address, "address");
        events.add(new OrderEvent.Created(id, address.building(), address.room(), 0, 0));
    }

    public UUID getId() {
        return id;
    }

    public Address address() {
        return address;
    }

    public int getBuilding() {
        return address.building();
    }

    public int getRoom() {
        return address.room();
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int version() {
        return version;
    }

    public boolean matchesStatus(OrderStatus expected) {
        int seen = version;
        boolean matches = status == expected;
        return seen == version && matches;
    }

    public int addPancake() {
        requireOpen();
        int pancakeId = nextPancakeId++;
        pancakes.add(new Pancake(pancakeId));
        bumpVersion();
        record(new OrderEvent.PancakeStarted(id, getBuilding(), getRoom(), pancakeId, pancakes.size(), version));
        return pancakeId;
    }

    public void addIngredient(Ingredient ingredient) {
        requireOpen();
        if (pancakes.isEmpty()) {
            throw new IllegalOrderStateException("Start a pancake before adding ingredients");
        }
        addIngredient(pancakes.get(pancakes.size() - 1).id(), ingredient);
    }

    public void addIngredient(int pancakeId, Ingredient ingredient) {
        requireOpen();
        pancake(pancakeId).addIngredient(ingredient);
        bumpVersion();
        record(new OrderEvent.IngredientAdded(
                id, getBuilding(), getRoom(), pancakeId, pancakeDescription(pancakeId), pancakes.size(), version));
    }

    public int removePancakes(String description, int count) {
        requireOpen();
        if (count <= 0) {
            throw new InvalidRemovalCountException();
        }
        Objects.requireNonNull(description, "description");
        int removed = 0;
        Iterator<Pancake> iterator = pancakes.iterator();
        while (iterator.hasNext() && removed < count) {
            if (iterator.next().description().equals(description)) {
                iterator.remove();
                removed++;
            }
        }
        bumpVersion();
        record(new OrderEvent.PancakesRemoved(
                id, getBuilding(), getRoom(), description, removed, pancakes.size(), version));
        return removed;
    }

    public List<String> pancakeDescriptions() {
        return pancakes.stream().map(Pancake::description).toList();
    }

    public String pancakeDescription(int pancakeId) {
        return pancake(pancakeId).description();
    }

    public int pancakeCount() {
        return pancakes.size();
    }

    private Pancake pancake(int pancakeId) {
        return pancakes.stream()
                .filter(pancake -> pancake.id() == pancakeId)
                .findFirst()
                .orElseThrow(() -> new PancakeNotFoundException(pancakeId));
    }

    public void complete() {
        if (!status.canTransitionTo(OrderStatus.COMPLETED)) {
            throw IllegalOrderStateException.unexpected(id, status, OrderStatus.COMPLETED);
        }
        if (pancakes.isEmpty() || pancakes.stream().anyMatch(Pancake::isEmpty)) {
            throw new IllegalOrderStateException("Order must contain pancakes with ingredients");
        }
        status = OrderStatus.COMPLETED;
        bumpVersion();
        record(new OrderEvent.Completed(id, getBuilding(), getRoom(), pancakes.size(), version));
    }

    public void prepare() {
        status = status.transitionTo(OrderStatus.PREPARED, id);
        bumpVersion();
        record(new OrderEvent.Prepared(id, getBuilding(), getRoom(), pancakes.size(), version));
    }

    public void markDelivered() {
        status = status.transitionTo(OrderStatus.DELIVERED, id);
        bumpVersion();
        record(new OrderEvent.Delivered(id, getBuilding(), getRoom(), pancakes.size(), version));
    }

    public void cancel() {
        status = status.transitionTo(OrderStatus.CANCELLED, id);
        bumpVersion();
        record(new OrderEvent.Cancelled(id, getBuilding(), getRoom(), pancakes.size(), version));
    }

    public List<OrderEvent> drainEvents() {
        if (events.isEmpty()) {
            return List.of();
        }
        List<OrderEvent> drained = List.copyOf(events);
        events.clear();
        return drained;
    }

    private void record(OrderEvent event) {
        events.add(event);
    }

    private void requireOpen() {
        if (!status.allowsEditing()) {
            throw IllegalOrderStateException.unexpected(id, status, OrderStatus.CREATED);
        }
    }

    private void bumpVersion() {
        version++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
