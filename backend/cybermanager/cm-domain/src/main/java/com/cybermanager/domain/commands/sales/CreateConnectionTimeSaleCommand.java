package com.cybermanager.domain.commands.sales;

import java.util.Set;
import java.util.UUID;

public record CreateConnectionTimeSaleCommand(String actorEmail, Set<String> actorRoles, UUID customerId, UUID sessionId, int minutes, boolean createDebt) {

}
