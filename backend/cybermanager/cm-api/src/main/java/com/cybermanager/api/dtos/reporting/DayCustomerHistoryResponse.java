package com.cybermanager.api.dtos.reporting;

import java.math.BigDecimal;
import java.util.UUID;

public record DayCustomerHistoryResponse(
        UUID customerId,
        String name,
        String type,
        int totalMinutes,
        BigDecimal salesTotal,
        BigDecimal debtTotal,
        BigDecimal collectedTotal,
        String state
) {

}
