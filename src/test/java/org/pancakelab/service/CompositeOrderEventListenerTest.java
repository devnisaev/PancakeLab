package org.pancakelab.service;

import org.junit.jupiter.api.Test;
import org.pancakelab.domain.OrderEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompositeOrderEventListenerTest {
    @Test
    void notifiesEveryListenerInOrder() {
        RecordingListener first = new RecordingListener();
        RecordingListener second = new RecordingListener();
        OrderEventListener composite = new CompositeOrderEventListener(first, second);
        OrderEvent.Created created = new OrderEvent.Created(UUID.randomUUID(), 1, 1, 0, 0);

        composite.handle(created);

        assertEquals(List.of(created), first.events);
        assertEquals(List.of(created), second.events);
    }

    @Test
    void rejectsEmptyListenerList() {
        assertThrows(IllegalArgumentException.class, () -> new CompositeOrderEventListener(List.of()));
    }

    private static final class RecordingListener implements OrderEventListener {
        private final List<OrderEvent> events = new ArrayList<>();

        @Override
        public void handle(OrderEvent event) {
            events.add(event);
        }
    }
}
