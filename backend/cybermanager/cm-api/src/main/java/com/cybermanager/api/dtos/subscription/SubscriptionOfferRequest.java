package com.cybermanager.api.dtos.subscription;

import java.math.BigDecimal;

public record SubscriptionOfferRequest(String name, BigDecimal price, int includedMinutes) {

}
