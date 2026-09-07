package org.pancakelab.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface Kitchen {
    List<String> viewOrder(UUID orderId);

    Set<UUID> listCompletedOrders();

    void prepareOrder(UUID orderId);
}
