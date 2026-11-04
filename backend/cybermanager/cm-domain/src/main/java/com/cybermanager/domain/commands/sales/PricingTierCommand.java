package com.cybermanager.domain.commands.sales;

import java.math.BigDecimal;

public record PricingTierCommand(int hours, int minutes, BigDecimal price) {

}
