package org.pancakelab.api;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record OrderTicket(UUID orderId, int building, int room, String status, List<String> pancakes) {
    public OrderTicket {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(status, "status");
        pancakes = List.copyOf(pancakes);
    }

    public int pancakeCount() {
        return pancakes.size();
    }
}
