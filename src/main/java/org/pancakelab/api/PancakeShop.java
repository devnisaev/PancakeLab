package org.pancakelab.api;

import java.util.List;

public interface PancakeShop extends DiscipleOrders, Kitchen, DeliveryDesk {
    List<OrderTicket> listOrders();

    void addMenuItem(String ingredient);
}
