package com.cybermanager.api.dtos.sales;

import java.math.BigDecimal;

public record InvoiceLineResponse(String label, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {

}
