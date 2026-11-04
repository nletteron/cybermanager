package com.cybermanager.domain.valueobjects.sales;

import com.cybermanager.domain.valueobjects.shared.Money;

public record InvoiceLine(
        String label,
        int quantity,
        Money unitPrice,
        Money totalPrice
) {

}
