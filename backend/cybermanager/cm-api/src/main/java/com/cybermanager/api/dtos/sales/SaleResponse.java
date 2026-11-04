package com.cybermanager.api.dtos.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleResponse(UUID saleId, UUID customerId, String type, LocalDateTime soldAt, List<SaleLineResponse> lines, BigDecimal totalAmount) {

}
