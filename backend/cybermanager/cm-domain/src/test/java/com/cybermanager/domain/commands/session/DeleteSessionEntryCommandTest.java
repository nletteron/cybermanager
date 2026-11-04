package com.cybermanager.domain.commands.session;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeleteSessionEntryCommandTest {

    @Test
    void shouldRequireSessionAndExactlyOneEntryIdentifier() {
        UUID sessionId = UUID.randomUUID();
        UUID saleId = UUID.randomUUID();
        UUID debtId = UUID.randomUUID();
        assertEquals(saleId, new DeleteSessionEntryCommand(sessionId, saleId, null).saleId());
        assertEquals(debtId, new DeleteSessionEntryCommand(sessionId, null, debtId).debtId());
        assertThrows(NullPointerException.class, () -> new DeleteSessionEntryCommand(null, saleId, null));
        assertThrows(IllegalArgumentException.class, () -> new DeleteSessionEntryCommand(sessionId, null, null));
        assertThrows(IllegalArgumentException.class, () -> new DeleteSessionEntryCommand(sessionId, saleId, debtId));
    }

}
