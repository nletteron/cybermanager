package com.cybermanager.domain.commands.sales;

import java.math.BigDecimal;

public record ManualInvoiceLineCommand(String label, int quantity, BigDecimal unitPrice) {

}
