package com.cybermanager.api.dtos.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryResponse(UUID categoryId, String name, String description, BigDecimal vatRate) {

}
