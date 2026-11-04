package com.cybermanager.domain.commands.catalog;
import java.math.BigDecimal;
import java.util.Set;
public record CreateCategoryCommand(Set<String> actorRoles, String name, String description, BigDecimal vatRate) {

}
