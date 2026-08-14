package org.pancakelab.service;

import org.pancakelab.domain.Order;

public class OrderLog {
    private final StringBuilder log = new StringBuilder();

    public synchronized void logAddPancake(Order order, String description) {
        log.append("Added pancake with description '%s' ".formatted(description))
           .append("to order %s containing %d pancakes, ".formatted(order.getId(), order.pancakeCount()))
           .append("for building %d, room %d.".formatted(order.getBuilding(), order.getRoom()));
    }

    public synchronized void logRemovePancakes(Order order, String description, int count) {
        log.append("Removed %d pancake(s) with description '%s' ".formatted(count, description))
           .append("from order %s now containing %d pancakes, ".formatted(order.getId(), order.pancakeCount()))
           .append("for building %d, room %d.".formatted(order.getBuilding(), order.getRoom()));
    }

    public synchronized void logCancelOrder(Order order) {
        log.append("Cancelled order %s with %d pancakes ".formatted(order.getId(), order.pancakeCount()))
           .append("for building %d, room %d.".formatted(order.getBuilding(), order.getRoom()));
    }

    public synchronized void logDeliverOrder(Order order) {
        log.append("Order %s with %d pancakes ".formatted(order.getId(), order.pancakeCount()))
           .append("for building %d, room %d out for delivery.".formatted(order.getBuilding(), order.getRoom()));
    }

    public synchronized String snapshot() {
        return log.toString();
    }
}
