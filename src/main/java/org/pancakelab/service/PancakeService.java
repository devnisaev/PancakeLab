package org.pancakelab.service;

import org.pancakelab.enums.OrderStatus;
import org.pancakelab.exception.IllegalOrderStateException;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.model.BuildingRegistry;
import org.pancakelab.model.IngredientCatalog;
import org.pancakelab.model.Location;
import org.pancakelab.model.Order;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PancakeService {
    private final ConcurrentHashMap<UUID, Order> orders = new ConcurrentHashMap<>();
    private final BuildingRegistry buildings;
    private final IngredientCatalog ingredients;
    private final OrderLog orderLog = new OrderLog();

    public PancakeService() {
        this(BuildingRegistry.dojoCampus(), IngredientCatalog.standard());
    }

    PancakeService(BuildingRegistry buildings, IngredientCatalog ingredients) {
        this.buildings = Objects.requireNonNull(buildings, "buildings");
        this.ingredients = Objects.requireNonNull(ingredients, "ingredients");
    }

    public List<String> listMenu() {
        return ingredients.names();
    }

    public UUID createOrder(int building, int room) {
        Location location = buildings.require(building, room);
        Order order = new Order(location);
        orders.put(order.getId(), order);
        return order.getId();
    }

    public int addPancake(UUID orderId) {
        Order order = requireOrder(orderId);
        synchronized (order) {
            requirePresent(orderId, order);
            return order.addPancake();
        }
    }

    public void addIngredient(UUID orderId, String ingredient) {
        Order order = requireOrder(orderId);
        synchronized (order) {
            requirePresent(orderId, order);
            order.addIngredient(ingredients.require(ingredient));
            orderLog.logAddPancake(order, order.pancakeDescriptions().get(order.pancakeCount() - 1));
        }
    }

    public void addIngredient(UUID orderId, int pancakeId, String ingredient) {
        Order order = requireOrder(orderId);
        synchronized (order) {
            requirePresent(orderId, order);
            order.addIngredient(pancakeId, ingredients.require(ingredient));
            orderLog.logAddPancake(order, order.pancakeDescription(pancakeId));
        }
    }

    public List<String> viewOrder(UUID orderId) {
        Order order = requireOrder(orderId);
        synchronized (order) {
            requirePresent(orderId, order);
            return order.pancakeDescriptions();
        }
    }

    public void removePancakes(String description, UUID orderId, int count) {
        Order order = requireOrder(orderId);
        synchronized (order) {
            requirePresent(orderId, order);
            int removed = order.removePancakes(description, count);
            orderLog.logRemovePancakes(order, description, removed);
        }
    }

    public void completeOrder(UUID orderId) {
        Order order = requireOrder(orderId);
        synchronized (order) {
            requirePresent(orderId, order);
            order.complete();
        }
    }

    public void cancelOrder(UUID orderId) {
        Order order = requireOrder(orderId);
        synchronized (order) {
            requirePresent(orderId, order);
            orderLog.logCancelOrder(order);
            order.cancel();
            orders.remove(orderId);
        }
    }

    public Set<UUID> listCompletedOrders() {
        return listByStatus(OrderStatus.COMPLETED);
    }

    public void prepareOrder(UUID orderId) {
        Order order = requireOrder(orderId);
        synchronized (order) {
            requirePresent(orderId, order);
            order.prepare();
        }
    }

    public Set<UUID> listPreparedOrders() {
        return listByStatus(OrderStatus.PREPARED);
    }

    public DeliveryResult deliverOrder(UUID orderId) {
        Order order = requireOrder(orderId);
        synchronized (order) {
            requirePresent(orderId, order);
            if (order.getStatus() != OrderStatus.PREPARED) {
                throw IllegalOrderStateException.unexpected(orderId, order.getStatus(), OrderStatus.PREPARED);
            }
            orderLog.logDeliverOrder(order);
            List<String> pancakes = order.pancakeDescriptions();
            order.markDelivered();
            orders.remove(orderId);
            return new DeliveryResult(order.getId(), order.getBuilding(), order.getRoom(), pancakes);
        }
    }

    private Set<UUID> listByStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(order -> {
                    synchronized (order) {
                        return order.getStatus() == status;
                    }
                })
                .map(Order::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Order requireOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId");
        Order order = orders.get(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        return order;
    }

    private void requirePresent(UUID orderId, Order order) {
        if (orders.get(orderId) != order) {
            throw new OrderNotFoundException(orderId);
        }
    }
}
