package com.cybermanager.api.dtos.customer;

import java.util.List;
import java.util.UUID;

public record CustomerDetailsResponse(
        UUID customerId,
        String name,
        String type,
        String status,
        int remainingMinutes,
        String currentSubscriptionLabel,
        List<CustomerPurchaseResponse> purchases,
        List<CustomerDebtResponse> debts
) {

}
