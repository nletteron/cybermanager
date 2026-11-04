package com.cybermanager.api.dtos.subscription;

import java.math.BigDecimal;
import java.util.UUID;

public record SubscriptionOfferResponse(UUID offerId, String name, BigDecimal price, int includedMinutes, String status) {

}
