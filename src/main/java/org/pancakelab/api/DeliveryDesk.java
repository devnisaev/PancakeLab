package org.pancakelab.api;

import java.util.Set;
import java.util.UUID;

public interface DeliveryDesk {
    Set<UUID> listPreparedOrders();

    DeliveryResult deliverOrder(UUID orderId);
}
