package com.cybermanager.api.dtos.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PricingTierResponse(Long id, int hours, int minutes, int durationMinutes, BigDecimal price, LocalDateTime createdAt, LocalDateTime updatedAt) {

}
