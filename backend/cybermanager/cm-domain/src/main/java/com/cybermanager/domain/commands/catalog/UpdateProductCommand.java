package com.cybermanager.domain.commands.catalog;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record UpdateProductCommand(String actorEmail, Set<String> actorRoles, UUID productId, String name, BigDecimal price, UUID categoryId, String description) {

}
