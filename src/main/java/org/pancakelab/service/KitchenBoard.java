package org.pancakelab.service;

import org.pancakelab.domain.OrderEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class KitchenBoard implements OrderEventListener {
    private final Set<UUID> awaitingPrep = ConcurrentHashMap.newKeySet();
    private final Set<UUID> readyForDelivery = ConcurrentHashMap.newKeySet();

    @Override
    public void handle(OrderEvent event) {
        if (event instanceof OrderEvent.Completed completed) {
            awaitingPrep.add(completed.orderId());
        } else if (event instanceof OrderEvent.Prepared prepared) {
            awaitingPrep.remove(prepared.orderId());
            readyForDelivery.add(prepared.orderId());
        } else if (event instanceof OrderEvent.Delivered delivered) {
            readyForDelivery.remove(delivered.orderId());
        } else if (event instanceof OrderEvent.Cancelled cancelled) {
            awaitingPrep.remove(cancelled.orderId());
            readyForDelivery.remove(cancelled.orderId());
        }
    }

    public Set<UUID> awaitingPrep() {
        return Set.copyOf(awaitingPrep);
    }

    public Set<UUID> readyForDelivery() {
        return Set.copyOf(readyForDelivery);
    }
}
