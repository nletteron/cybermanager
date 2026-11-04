package com.cybermanager.domain.commands.catalog;

import java.util.Set;
import java.util.UUID;

public record ActivateProductCommand(String actorEmail, Set<String> actorRoles, UUID productId) {

}
