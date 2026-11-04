package com.cybermanager.api.dtos.monitoring;

import java.math.BigDecimal;

public record SaleActivityResponse(String label, int quantity, BigDecimal totalPrice, boolean debt, String soldAt) {

}
