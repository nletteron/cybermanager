package com.cybermanager.api.dtos.reporting;

import java.math.BigDecimal;

public record SaleActivityResponse(String label, int quantity, BigDecimal totalPrice) {

}
