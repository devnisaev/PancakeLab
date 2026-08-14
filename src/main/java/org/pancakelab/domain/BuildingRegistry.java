package org.pancakelab.domain;

import org.pancakelab.exception.InvalidLocationException;

public final class BuildingRegistry {
    private final int minBuilding;
    private final int maxBuilding;
    private final int minRoom;
    private final int maxRoom;

    public BuildingRegistry(int minBuilding, int maxBuilding, int minRoom, int maxRoom) {
        if (minBuilding > maxBuilding || minRoom > maxRoom) {
            throw new IllegalArgumentException("Invalid building or room range");
        }
        this.minBuilding = minBuilding;
        this.maxBuilding = maxBuilding;
        this.minRoom = minRoom;
        this.maxRoom = maxRoom;
    }

    public static BuildingRegistry dojoCampus() {
        return new BuildingRegistry(1, 20, 1, 100);
    }

    public Location require(int building, int room) {
        if (building < minBuilding || building > maxBuilding) {
            throw new InvalidLocationException("Building %d does not exist".formatted(building));
        }
        if (room < minRoom || room > maxRoom) {
            throw new InvalidLocationException(
                    "Room %d does not exist in building %d".formatted(room, building));
        }
        return new Location(building, room);
    }
}
