package com.cybermanager.api.dtos.session;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaySessionRequest(BigDecimal amountPaid, List<UUID> subscriptionOfferIds, boolean createSubscriptionDebt) {

}
