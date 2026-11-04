package com.cybermanager.api.dtos.monitoring;

import java.math.BigDecimal;
import java.util.UUID;

public record DayCustomerResponse(
        UUID customerId,
        String name,
        String type,
        int remainingMinutes,
        int consumedMinutes,
        BigDecimal purchasesTotal,
        BigDecimal debtTotal,
        BigDecimal collectedTotal,
        String state
) {

}
