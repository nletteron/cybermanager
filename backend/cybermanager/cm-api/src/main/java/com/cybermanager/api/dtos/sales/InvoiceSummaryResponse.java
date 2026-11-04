package com.cybermanager.api.dtos.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceSummaryResponse(
        UUID invoiceId,
        UUID saleId,
        String invoiceNumber,
        String customerName,
        LocalDateTime issuedAt,
        String status,
        BigDecimal totalAmount
) {

}
