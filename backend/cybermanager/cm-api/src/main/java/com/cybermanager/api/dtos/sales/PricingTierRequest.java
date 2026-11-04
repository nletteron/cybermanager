package com.cybermanager.api.dtos.sales;

import java.math.BigDecimal;

public record PricingTierRequest(int hours, int minutes, BigDecimal price) {

}
