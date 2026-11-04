package com.cybermanager.api.dtos.sales;

import java.util.UUID;

public record ConnectionTimeSaleRequest(UUID customerId, UUID sessionId, int minutes, boolean createDebt) {

}
