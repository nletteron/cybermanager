package com.cybermanager.api.dtos.sales;

import java.util.UUID;

public record SubscriptionSaleRequest(UUID customerId, UUID sessionId, UUID subscriptionOfferId, Integer quantity, boolean createDebt) {

}
