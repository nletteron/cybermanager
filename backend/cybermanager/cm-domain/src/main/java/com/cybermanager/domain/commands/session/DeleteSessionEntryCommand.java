package com.cybermanager.domain.commands.session;

import java.util.Objects;
import java.util.UUID;

public record DeleteSessionEntryCommand(UUID sessionId, UUID saleId, UUID debtId) {

    public DeleteSessionEntryCommand {
        Objects.requireNonNull(sessionId, "sessionId is required");
        if ((saleId == null) == (debtId == null)) {
            throw new IllegalArgumentException("Exactly one saleId or debtId is required");
        }
    }

}
