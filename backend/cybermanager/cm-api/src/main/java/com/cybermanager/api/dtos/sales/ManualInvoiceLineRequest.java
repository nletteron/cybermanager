package com.cybermanager.api.dtos.sales;

import java.math.BigDecimal;

public record ManualInvoiceLineRequest(String label, int quantity, BigDecimal unitPrice) {

}
