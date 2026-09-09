package org.pancakelab.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pancakelab.domain.OrderEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileOrderArchiveTest {
    private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant TS = Instant.parse("2026-09-09T13:58:00Z");
    private static final List<String> PANCAKES = List.of("Delicious pancake with dark chocolate!");

    @TempDir
    Path root;

    @Test
    void appendsImportantEventsAndSkipsToppingNoise() throws Exception {
        FileOrderArchive archive = archive();
        archive.handle(new OrderEvent.Created(ORDER_ID, 10, 20, 0, 0));
        archive.handle(new OrderEvent.PancakeStarted(ORDER_ID, 10, 20, 0, 1, 1));
        archive.handle(new OrderEvent.IngredientAdded(
                ORDER_ID, 10, 20, 0, PANCAKES.get(0), 1, 2));
        archive.handle(new OrderEvent.Completed(ORDER_ID, 10, 20, 1, 3));

        String events = Files.readString(root.resolve("events.log"));
        assertTrue(events.contains("event=Created"));
        assertTrue(events.contains("event=Completed"));
        assertFalse(events.contains("event=PancakeStarted"));
        assertFalse(events.contains("event=IngredientAdded"));
        assertFalse(Files.exists(root.resolve("cancelled.log")));
        assertFalse(Files.exists(root.resolve("delivered.log")));
    }

    @Test
    void copiesCancelledOrdersToTheirOwnLog() throws Exception {
        FileOrderArchive archive = archive();
        archive.handle(new OrderEvent.Cancelled(ORDER_ID, 10, 20, 1, 4));

        String cancelled = Files.readString(root.resolve("cancelled.log"));
        assertTrue(cancelled.contains("event=Cancelled"));
        assertTrue(cancelled.contains(ORDER_ID.toString()));
        assertTrue(Files.readString(root.resolve("events.log")).contains("event=Cancelled"));
    }

    @Test
    void writesDeliveredLogAndAStatementListingPancakes() throws Exception {
        FileOrderArchive archive = archive();
        archive.handle(new OrderEvent.Delivered(ORDER_ID, 10, 20, 1, 5, PANCAKES));

        String delivered = Files.readString(root.resolve("delivered.log"));
        assertTrue(delivered.contains("event=Delivered"));
        String statement = Files.readString(root.resolve("statements").resolve(ORDER_ID + ".txt"));
        assertTrue(statement.contains("Pancake Lab delivery statement"));
        assertTrue(statement.contains("time=2026-09-09T13:58:00Z"));
        assertTrue(statement.contains("building=10"));
        assertTrue(statement.contains("room=20"));
        assertTrue(statement.contains("- Delicious pancake with dark chocolate!"));
    }

    @Test
    void doesNotFailTheCallerWhenTheRootCannotBeWritten() throws Exception {
        Path blocked = Files.createTempFile("shop-archive", ".file");
        FileOrderArchive archive = new FileOrderArchive(blocked, Clock.fixed(TS, ZoneOffset.UTC));
        archive.handle(new OrderEvent.Cancelled(ORDER_ID, 1, 1, 0, 1));
        Files.deleteIfExists(blocked);
    }

    private FileOrderArchive archive() {
        return new FileOrderArchive(root, Clock.fixed(TS, ZoneOffset.UTC));
    }
}
