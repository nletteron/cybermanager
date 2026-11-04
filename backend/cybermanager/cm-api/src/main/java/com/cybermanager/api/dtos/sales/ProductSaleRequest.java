package com.cybermanager.api.dtos.sales;

import java.util.List;
import java.util.UUID;

public record ProductSaleRequest(UUID customerId, UUID sessionId, List<ProductSaleLineRequest> lines, boolean createDebt) {

}
