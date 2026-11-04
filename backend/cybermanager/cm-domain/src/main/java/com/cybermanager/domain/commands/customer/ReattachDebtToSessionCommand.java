package com.cybermanager.domain.commands.customer;

import java.util.Set;
import java.util.UUID;

public record ReattachDebtToSessionCommand(
        String actorEmail,
        Set<String> actorRoles,
        UUID debtId,
        UUID sessionId
) {

}
