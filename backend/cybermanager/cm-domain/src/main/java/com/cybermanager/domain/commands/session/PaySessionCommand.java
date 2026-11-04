package com.cybermanager.domain.commands.session;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaySessionCommand(
        UUID sessionId,
        BigDecimal amountPaid,
        List<UUID> subscriptionOfferIds,
        boolean createSubscriptionDebt
) {

}
