package org.pancakelab.domain;

public record Address(int building, int room) {
    public Address {
        if (building < 1) {
            throw new IllegalArgumentException("Building must be positive");
        }
        if (room < 1) {
            throw new IllegalArgumentException("Room must be positive");
        }
    }

    @Override
    public String toString() {
        return "building %d, room %d".formatted(building, room);
    }
}
