package com.cybermanager.domain.businessobjects.sales;

import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.InvoiceLine;
import com.cybermanager.domain.valueobjects.sales.InvoiceStatus;
import com.cybermanager.domain.valueobjects.shared.Money;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record Invoice(
        UUID id,
        UUID saleId,
        String invoiceNumber,
        CustomerId customerId,
        String customerName,
        LocalDateTime issuedAt,
        InvoiceStatus status,
        Money totalAmount,
        List<InvoiceLine> lines
) {

}
