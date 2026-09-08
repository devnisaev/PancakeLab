package org.pancakelab.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.pancakelab.exception.InvalidLocationException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressRegistryTest {
    private final AddressRegistry campus = AddressRegistry.dojoCampus();

    @Test
    void acceptsKnownBuildingAndRoom() {
        Address address = campus.require(10, 20);
        assertEquals(10, address.building());
        assertEquals(20, address.room());
    }

    @Test
    void campusListsRegisteredBuildings() {
        assertEquals(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20),
                campus.buildings());
    }

    @Test
    void eachBuildingHasItsOwnRoomCapacity() {
        assertTrue(campus.require(3, 50).room() == 50);
        assertTrue(campus.require(10, 80).room() == 80);
        assertTrue(campus.require(18, 100).room() == 100);
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "21, 1", "-3, 10", "99, 1"})
    void rejectsUnknownBuilding(int building, int room) {
        InvalidLocationException exception =
                assertThrows(InvalidLocationException.class, () -> campus.require(building, room));
        assertEquals("Building %d does not exist".formatted(building), exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"1, 0", "1, 51", "10, 81", "18, 101", "10, -1"})
    void rejectsRoomOutsideBuildingCapacity(int building, int room) {
        InvalidLocationException exception =
                assertThrows(InvalidLocationException.class, () -> campus.require(building, room));
        assertEquals("Room %d does not exist in building %d".formatted(room, building), exception.getMessage());
    }

    @Test
    void reportsWhetherBuildingExists() {
        assertTrue(campus.hasBuilding(1));
        assertFalse(campus.hasBuilding(99));
    }
}
