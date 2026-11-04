package com.cybermanager.api.dtos.reporting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DayHistoryResponse(LocalDate startDate, LocalDate endDate, List<DayCustomerHistoryResponse> customers, BigDecimal totalCollected, BigDecimal totalDebtCreated) {

}
