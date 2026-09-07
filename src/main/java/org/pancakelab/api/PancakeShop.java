package org.pancakelab.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PancakeShop {

    List<String> listMenu();

    UUID createOrder(int building, int room);

    int addPancake(UUID orderId);

    void addIngredient(UUID orderId, String ingredient);

    void addIngredient(UUID orderId, int pancakeId, String ingredient);

    List<String> viewOrder(UUID orderId);

    void removePancakes(String description, UUID orderId, int count);

    void completeOrder(UUID orderId);

    void cancelOrder(UUID orderId);

    Set<UUID> listCompletedOrders();

    void prepareOrder(UUID orderId);

    Set<UUID> listPreparedOrders();

    DeliveryResult deliverOrder(UUID orderId);
}
