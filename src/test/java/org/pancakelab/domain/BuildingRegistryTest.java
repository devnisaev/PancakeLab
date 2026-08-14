package org.pancakelab.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.pancakelab.exception.InvalidLocationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuildingRegistryTest {
    private final BuildingRegistry campus = BuildingRegistry.dojoCampus();

    @Test
    void acceptsExistingBuildingAndRoom() {
        Location location = campus.require(10, 20);
        assertEquals(10, location.building());
        assertEquals(20, location.room());
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "21, 1", "-3, 10"})
    void rejectsMissingBuilding(int building, int room) {
        InvalidLocationException exception =
                assertThrows(InvalidLocationException.class, () -> campus.require(building, room));
        assertEquals("Building %d does not exist".formatted(building), exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"1, 0", "1, 101", "10, -1"})
    void rejectsMissingRoom(int building, int room) {
        InvalidLocationException exception =
                assertThrows(InvalidLocationException.class, () -> campus.require(building, room));
        assertEquals("Room %d does not exist in building %d".formatted(room, building), exception.getMessage());
    }
}
