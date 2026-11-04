package com.cybermanager.application.views.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PricingTierView(Long id, int hours, int minutes, int durationMinutes, BigDecimal price, LocalDateTime createdAt, LocalDateTime updatedAt) {

}
