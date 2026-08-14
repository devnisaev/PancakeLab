package org.pancakelab.domain;

import java.util.Objects;

public final class Location {
    private final int building;
    private final int room;

    Location(int building, int room) {
        this.building = building;
        this.room = room;
    }

    public int building() {
        return building;
    }

    public int room() {
        return room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return building == location.building && room == location.room;
    }

    @Override
    public int hashCode() {
        return Objects.hash(building, room);
    }

    @Override
    public String toString() {
        return "building %d, room %d".formatted(building, room);
    }
}
