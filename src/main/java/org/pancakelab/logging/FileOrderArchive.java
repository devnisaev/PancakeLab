package org.pancakelab.logging;

import org.pancakelab.domain.OrderEvent;
import org.pancakelab.service.OrderEventListener;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public final class FileOrderArchive implements OrderEventListener {
    private static final String EVENTS = "events.log";
    private static final String CANCELLED = "cancelled.log";
    private static final String DELIVERED = "delivered.log";

    private final Path root;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    public FileOrderArchive(Path root) {
        this(root, Clock.systemUTC());
    }

    FileOrderArchive(Path root, Clock clock) {
        this.root = Objects.requireNonNull(root, "root").toAbsolutePath();
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public static FileOrderArchive inWorkingDirectory() {
        return new FileOrderArchive(Path.of("shop-log"));
    }

    @Override
    public void handle(OrderEvent event) {
        Objects.requireNonNull(event, "event");
        if (!important(event)) {
            return;
        }
        lock.lock();
        try {
            archive(event);
        } catch (RuntimeException | IOException ignored) {
            System.err.println("Failed to archive " + event.getClass().getSimpleName());
        } finally {
            lock.unlock();
        }
    }

    private static boolean important(OrderEvent event) {
        return !(event instanceof OrderEvent.PancakeStarted
                || event instanceof OrderEvent.IngredientAdded
                || event instanceof OrderEvent.PancakesRemoved);
    }

    private void archive(OrderEvent event) throws IOException {
        Files.createDirectories(root);
        append(EVENTS, line(event));
        if (event instanceof OrderEvent.Cancelled) {
            append(CANCELLED, line(event));
        }
        if (event instanceof OrderEvent.Delivered delivered) {
            append(DELIVERED, line(event));
            writeStatement(delivered);
        }
    }

    private void writeStatement(OrderEvent.Delivered delivered) throws IOException {
        Path statements = root.resolve("statements");
        Files.createDirectories(statements);
        Files.writeString(statements.resolve(delivered.orderId() + ".txt"), statement(delivered), StandardCharsets.UTF_8);
    }

    private String statement(OrderEvent.Delivered delivered) {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder();
        text.append("Pancake Lab delivery statement").append(nl);
        text.append("time=").append(clock.instant()).append(nl);
        text.append("orderId=").append(delivered.orderId()).append(nl);
        text.append("building=").append(delivered.building()).append(nl);
        text.append("room=").append(delivered.room()).append(nl);
        text.append("pancakes:").append(nl);
        for (String pancake : delivered.pancakes()) {
            text.append("- ").append(pancake).append(nl);
        }
        return text.toString();
    }

    private void append(String fileName, String line) throws IOException {
        Files.writeString(
                root.resolve(fileName),
                line + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE);
    }

    private String line(OrderEvent event) {
        return Logfmt.line(clock.instant(), Thread.currentThread().getName(), Level.INFO, event);
    }
}
