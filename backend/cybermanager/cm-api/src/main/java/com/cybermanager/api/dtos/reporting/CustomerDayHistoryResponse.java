package com.cybermanager.api.dtos.reporting;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CustomerDayHistoryResponse(UUID customerId, String name, List<SaleActivityResponse> sales, List<String> sessions, BigDecimal totalCollected, BigDecimal totalDebtCreated) {

}
