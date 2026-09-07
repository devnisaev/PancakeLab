package org.pancakelab.service;

import org.pancakelab.api.DeliveryResult;
import org.pancakelab.api.OrderTicket;
import org.pancakelab.api.PancakeShop;
import org.pancakelab.domain.BuildingRegistry;
import org.pancakelab.domain.Ingredient;
import org.pancakelab.domain.IngredientCatalog;
import org.pancakelab.domain.Location;
import org.pancakelab.domain.Order;
import org.pancakelab.domain.OrderEvent;
import org.pancakelab.domain.OrderRepository;
import org.pancakelab.enums.OrderStatus;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.logging.DiscardingShopJournal;
import org.pancakelab.logging.ShopJournal;
import org.pancakelab.logging.SystemShopJournal;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class PancakeService implements PancakeShop {
    private final OrderRepository orders;
    private final BuildingRegistry buildings;
    private final IngredientCatalog ingredients;
    private final ShopJournal journal;

    public PancakeService() {
        this(BuildingRegistry.dojoCampus(), IngredientCatalog.standard());
    }

    public static PancakeService logged() {
        return new PancakeService(
                BuildingRegistry.dojoCampus(),
                IngredientCatalog.standard(),
                new InMemoryOrderRepository(),
                new SystemShopJournal());
    }

    PancakeService(BuildingRegistry buildings, IngredientCatalog ingredients) {
        this(buildings, ingredients, new InMemoryOrderRepository());
    }

    PancakeService(BuildingRegistry buildings, IngredientCatalog ingredients, OrderRepository orders) {
        this(buildings, ingredients, orders, DiscardingShopJournal.INSTANCE);
    }

    PancakeService(
            BuildingRegistry buildings, IngredientCatalog ingredients, OrderRepository orders, ShopJournal journal) {
        this.buildings = Objects.requireNonNull(buildings, "buildings");
        this.ingredients = Objects.requireNonNull(ingredients, "ingredients");
        this.orders = Objects.requireNonNull(orders, "orders");
        this.journal = Objects.requireNonNull(journal, "journal");
    }

    @Override
    public List<String> listMenu() {
        return List.copyOf(ingredients.names());
    }

    @Override
    public UUID createOrder(int building, int room) {
        Location location = buildings.require(building, room);
        Order order = new Order(location);
        orders.save(order);
        journal.recordAll(order.drainEvents());
        return order.getId();
    }

    @Override
    public int addPancake(UUID orderId) {
        return change(orderId, Order::addPancake);
    }

    @Override
    public void addIngredient(UUID orderId, String ingredient) {
        Ingredient allowed = ingredients.require(ingredient);
        change(orderId, order -> {
            order.addIngredient(allowed);
            return null;
        });
    }

    @Override
    public void addIngredient(UUID orderId, int pancakeId, String ingredient) {
        Ingredient allowed = ingredients.require(ingredient);
        change(orderId, order -> {
            order.addIngredient(pancakeId, allowed);
            return null;
        });
    }

    @Override
    public List<String> viewOrder(UUID orderId) {
        return orders.withOrder(orderId, order -> List.copyOf(order.pancakeDescriptions()));
    }

    @Override
    public void removePancakes(String description, UUID orderId, int count) {
        change(orderId, order -> order.removePancakes(description, count));
    }

    @Override
    public void completeOrder(UUID orderId) {
        change(orderId, order -> {
            order.complete();
            return null;
        });
    }

    @Override
    public void cancelOrder(UUID orderId) {
        take(orderId, order -> {
            order.cancel();
            return null;
        });
    }

    @Override
    public Set<UUID> listCompletedOrders() {
        return orders.idsWithStatus(OrderStatus.COMPLETED);
    }

    @Override
    public void prepareOrder(UUID orderId) {
        change(orderId, order -> {
            order.prepare();
            return null;
        });
    }

    @Override
    public Set<UUID> listPreparedOrders() {
        return orders.idsWithStatus(OrderStatus.PREPARED);
    }

    @Override
    public DeliveryResult deliverOrder(UUID orderId) {
        return take(orderId, order -> {
            order.markDelivered();
            List<String> pancakes = order.pancakeDescriptions();
            return new DeliveryResult(order.getId(), order.getBuilding(), order.getRoom(), pancakes);
        });
    }

    @Override
    public List<OrderTicket> listOrders() {
        return orders.ids().stream()
                .map(this::ticketOrGone)
                .flatMap(Optional::stream)
                .sorted(ticketOrder())
                .toList();
    }

    @Override
    public void addMenuItem(String ingredient) {
        ingredients.add(ingredient);
    }

    private static Comparator<OrderTicket> ticketOrder() {
        return Comparator.comparingInt(OrderTicket::building)
                .thenComparingInt(OrderTicket::room)
                .thenComparing(OrderTicket::orderId);
    }

    private Optional<OrderTicket> ticketOrGone(UUID orderId) {
        try {
            return Optional.of(orders.withOrder(orderId, this::ticket));
        } catch (OrderNotFoundException ignored) {
            return Optional.empty();
        }
    }

    private OrderTicket ticket(Order order) {
        return new OrderTicket(
                order.getId(),
                order.getBuilding(),
                order.getRoom(),
                order.getStatus().name(),
                order.pancakeDescriptions());
    }

    private <T> T change(UUID orderId, Function<Order, T> action) {
        return publish(orders.withOrder(orderId, order -> run(order, action)));
    }

    private <T> T take(UUID orderId, Function<Order, T> action) {
        return publish(orders.take(orderId, order -> run(order, action)));
    }

    private <T> Outcome<T> run(Order order, Function<Order, T> action) {
        try {
            return new Outcome<>(action.apply(order), order.drainEvents());
        } catch (RuntimeException exception) {
            order.drainEvents();
            throw exception;
        }
    }

    private <T> T publish(Outcome<T> outcome) {
        journal.recordAll(outcome.events());
        return outcome.value();
    }

    private record Outcome<T>(T value, List<OrderEvent> events) {
        private Outcome {
            events = List.copyOf(events);
        }
    }
}
