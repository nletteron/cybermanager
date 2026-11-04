package com.cybermanager.api.dtos.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ArchiveCustomerCandidateResponse(
        UUID customerId,
        String name,
        String type,
        String status,
        int remainingMinutes,
        LocalDateTime latestActivityAt,
        int sessionCount,
        int saleCount,
        int debtCount,
        BigDecimal salesTotal,
        BigDecimal debtTotal
) {

}
