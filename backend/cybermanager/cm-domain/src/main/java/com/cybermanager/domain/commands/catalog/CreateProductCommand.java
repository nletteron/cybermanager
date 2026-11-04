package com.cybermanager.domain.commands.catalog;

import java.math.BigDecimal;
import java.util.Set;

public record CreateProductCommand(String actorEmail, Set<String> actorRoles, String name, BigDecimal price, java.util.UUID categoryId, String description) {

}
