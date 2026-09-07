package org.pancakelab.logging;

import org.junit.jupiter.api.Test;
import org.pancakelab.domain.OrderEvent;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundedShopJournalTest {
    @Test
    void dropsOldestEventsWhenCapacityIsReached() {
        BoundedShopJournal journal = new BoundedShopJournal(2);
        OrderEvent first = created("11111111-1111-1111-1111-111111111111");
        OrderEvent second = created("22222222-2222-2222-2222-222222222222");
        OrderEvent third = created("33333333-3333-3333-3333-333333333333");

        journal.record(first);
        journal.record(second);
        journal.record(third);

        assertEquals(2, journal.recent().size());
        assertEquals(second, journal.recent().get(0));
        assertEquals(third, journal.recent().get(1));
    }

    @Test
    void recentSnapshotCannotBeMutated() {
        BoundedShopJournal journal = new BoundedShopJournal(1);
        journal.record(created("11111111-1111-1111-1111-111111111111"));
        assertThrows(UnsupportedOperationException.class,
                () -> journal.recent().add(created("22222222-2222-2222-2222-222222222222")));
    }

    @Test
    void rejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new BoundedShopJournal(0));
    }

    private static OrderEvent created(String id) {
        return new OrderEvent.Created(UUID.fromString(id), 1, 1, 0, 0);
    }
}
