package org.pancakelab.model;

import org.pancakelab.enums.OrderStatus;
import org.pancakelab.exception.IllegalOrderStateException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Order {
    private final UUID id;
    private final Location location;
    private final List<Pancake> pancakes = new ArrayList<>();
    private OrderStatus status = OrderStatus.CREATED;
    private int nextPancakeId = 0;

    public Order(Location location) {
        this(UUID.randomUUID(), location);
    }

    Order(UUID id, Location location) {
        this.id = Objects.requireNonNull(id, "id");
        this.location = Objects.requireNonNull(location, "location");
    }

    public UUID getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public int getBuilding() {
        return location.building();
    }

    public int getRoom() {
        return location.room();
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int addPancake() {
        requireStatus(OrderStatus.CREATED);
        int pancakeId = nextPancakeId++;
        pancakes.add(new Pancake(pancakeId));
        return pancakeId;
    }

    public void addIngredient(Ingredient ingredient) {
        requireStatus(OrderStatus.CREATED);
        if (pancakes.isEmpty()) {
            throw new IllegalOrderStateException("Start a pancake before adding ingredients");
        }
        addIngredient(pancakes.get(pancakes.size() - 1).id(), ingredient);
    }

    public void addIngredient(int pancakeId, Ingredient ingredient) {
        requireStatus(OrderStatus.CREATED);
        pancake(pancakeId).addIngredient(ingredient);
    }

    public int removePancakes(String description, int count) {
        requireStatus(OrderStatus.CREATED);
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
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
                .orElseThrow(() -> new IllegalArgumentException("Pancake %d does not exist".formatted(pancakeId)));
    }

    public void complete() {
        requireStatus(OrderStatus.CREATED);
        if (pancakes.isEmpty() || pancakes.stream().anyMatch(Pancake::isEmpty)) {
            throw new IllegalOrderStateException("Order must contain pancakes with ingredients");
        }
        status = OrderStatus.COMPLETED;
    }

    public void prepare() {
        requireStatus(OrderStatus.COMPLETED);
        status = OrderStatus.PREPARED;
    }

    public void markDelivered() {
        requireStatus(OrderStatus.PREPARED);
        status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw IllegalOrderStateException.unexpected(id, status, OrderStatus.CREATED);
        }
        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw IllegalOrderStateException.unexpected(id, status, expected);
        }
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
