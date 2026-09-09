package org.pancakelab.domain;

import java.util.List;
import java.util.UUID;

public sealed interface OrderEvent {
    UUID orderId();

    int building();

    int room();

    int pancakeCount();

    int version();

    record Created(UUID orderId, int building, int room, int pancakeCount, int version) implements OrderEvent {}

    record PancakeStarted(UUID orderId, int building, int room, int pancakeId, int pancakeCount, int version)
            implements OrderEvent {}

    record IngredientAdded(
            UUID orderId, int building, int room, int pancakeId, String description, int pancakeCount, int version)
            implements OrderEvent {}

    record PancakesRemoved(
            UUID orderId, int building, int room, String description, int removed, int pancakeCount, int version)
            implements OrderEvent {}

    record Completed(UUID orderId, int building, int room, int pancakeCount, int version) implements OrderEvent {}

    record Prepared(UUID orderId, int building, int room, int pancakeCount, int version) implements OrderEvent {}

    record Cancelled(UUID orderId, int building, int room, int pancakeCount, int version) implements OrderEvent {}

    record Delivered(
            UUID orderId, int building, int room, int pancakeCount, int version, List<String> pancakes)
            implements OrderEvent {
        public Delivered {
            pancakes = List.copyOf(pancakes);
        }
    }
}
