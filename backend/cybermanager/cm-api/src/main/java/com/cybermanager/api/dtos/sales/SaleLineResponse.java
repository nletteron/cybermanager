package com.cybermanager.api.dtos.sales;

import java.math.BigDecimal;

public record SaleLineResponse(String label, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {

}
