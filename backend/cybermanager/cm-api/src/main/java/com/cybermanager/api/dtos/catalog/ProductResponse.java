package com.cybermanager.api.dtos.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(UUID productId, String name, BigDecimal price, UUID categoryId, String category, String description, String status) {

}
