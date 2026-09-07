package org.pancakelab.logging;

import org.junit.jupiter.api.Test;
import org.pancakelab.domain.OrderEvent;

import java.lang.System.Logger.Level;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogfmtTest {
    private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant TS = Instant.parse("2026-09-07T12:00:00Z");

    @Test
    void writesStableKeyValueAuditLine() {
        OrderEvent.Delivered event = new OrderEvent.Delivered(ORDER_ID, 10, 20, 3, 8);

        String line = Logfmt.line(TS, "shop-thread", Level.INFO, event);

        assertEquals(
                "ts=2026-09-07T12:00:00Z level=INFO event=Delivered orderId=11111111-1111-1111-1111-111111111111 "
                        + "building=10 room=20 pancakeCount=3 version=8 thread=shop-thread",
                line);
    }

    @Test
    void quotesDescriptionsThatContainSpaces() {
        OrderEvent.IngredientAdded event = new OrderEvent.IngredientAdded(
                ORDER_ID, 1, 1, 0, "Delicious pancake with dark chocolate!", 1, 2);

        String line = Logfmt.line(TS, "main", Level.INFO, event);

        assertTrue(line.contains("pancakeId=0"));
        assertTrue(line.contains("description=\"Delicious pancake with dark chocolate!\""));
        assertFalse(line.contains("description=Delicious pancake"));
    }
}
