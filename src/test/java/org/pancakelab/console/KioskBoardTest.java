package org.pancakelab.console;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KioskBoardTest {
    @Test
    void shortensPancakeDescriptionsToToppings() {
        assertEquals("dark chocolate, hazelnuts",
                KioskBoard.toppingsOf("Delicious pancake with dark chocolate, hazelnuts!"));
        assertEquals("(plain pancake)", KioskBoard.toppingsOf("Delicious pancake!"));
    }

    @Test
    void mapsStatusToBoardLabels() {
        assertEquals("OPEN", KioskBoard.label("CREATED"));
        assertEquals("KITCHEN", KioskBoard.label("COMPLETED"));
        assertEquals("READY", KioskBoard.label("PREPARED"));
        assertEquals("SENT", KioskBoard.label("DELIVERED"));
    }
}
