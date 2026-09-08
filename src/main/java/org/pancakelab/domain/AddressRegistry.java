package org.pancakelab.domain;

import org.pancakelab.exception.InvalidLocationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class AddressRegistry {
    public record RoomRange(int minRoom, int maxRoom) {
        public RoomRange {
            if (minRoom < 1 || maxRoom < minRoom) {
                throw new IllegalArgumentException("Invalid room range");
            }
        }

        boolean contains(int room) {
            return room >= minRoom && room <= maxRoom;
        }
    }

    private final Map<Integer, RoomRange> buildings;

    public AddressRegistry(Map<Integer, RoomRange> buildings) {
        if (buildings == null || buildings.isEmpty()) {
            throw new IllegalArgumentException("Campus must define at least one building");
        }
        this.buildings = Map.copyOf(buildings);
    }

    public static AddressRegistry dojoCampus() {
        Map<Integer, RoomRange> campus = new LinkedHashMap<>();
        for (int building = 1; building <= 20; building++) {
            int maxRoom = maxRoomFor(building);
            campus.put(building, new RoomRange(1, maxRoom));
        }
        return new AddressRegistry(campus);
    }

    public Address require(int building, int room) {
        RoomRange rooms = buildings.get(building);
        if (rooms == null) {
            throw new InvalidLocationException("Building %d does not exist".formatted(building));
        }
        if (!rooms.contains(room)) {
            throw new InvalidLocationException(
                    "Room %d does not exist in building %d".formatted(room, building));
        }
        return new Address(building, room);
    }

    public boolean hasBuilding(int building) {
        return buildings.containsKey(building);
    }

    public Set<Integer> buildings() {
        return Set.copyOf(buildings.keySet());
    }

    public int maxRoomIn(int building) {
        RoomRange rooms = buildings.get(building);
        if (rooms == null) {
            throw new InvalidLocationException("Building %d does not exist".formatted(building));
        }
        return rooms.maxRoom();
    }

    private static int maxRoomFor(int building) {
        if (building <= 5) {
            return 50;
        }
        if (building <= 15) {
            return 80;
        }
        return 100;
    }
}
