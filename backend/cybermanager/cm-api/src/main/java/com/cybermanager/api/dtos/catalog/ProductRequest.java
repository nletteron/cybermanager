package com.cybermanager.api.dtos.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(String name, BigDecimal price, UUID categoryId, String description) {

}
