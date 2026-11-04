package com.cybermanager.api.dtos.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceDetailResponse(
        UUID invoiceId,
        UUID saleId,
        UUID customerId,
        String invoiceNumber,
        String customerName,
        LocalDateTime issuedAt,
        String status,
        BigDecimal totalAmount,
        List<InvoiceLineResponse> lines
) {

}
