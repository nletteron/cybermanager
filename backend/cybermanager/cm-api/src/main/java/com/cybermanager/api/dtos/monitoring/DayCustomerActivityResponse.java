package com.cybermanager.api.dtos.monitoring;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DayCustomerActivityResponse(
        UUID customerId,
        String name,
        List<SaleActivityResponse> sales,
        List<SessionActivityResponse> sessions,
        BigDecimal totalCollected,
        BigDecimal totalDebtCreated
) {

}
