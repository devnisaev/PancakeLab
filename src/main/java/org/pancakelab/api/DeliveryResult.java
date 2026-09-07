package org.pancakelab.api;

import java.util.List;
import java.util.UUID;

public record DeliveryResult(UUID orderId, int building, int room, List<String> pancakes) {
    public DeliveryResult {
        pancakes = List.copyOf(pancakes);
    }
}
