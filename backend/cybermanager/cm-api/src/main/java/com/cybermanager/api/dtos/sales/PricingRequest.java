package com.cybermanager.api.dtos.sales;

import java.util.List;

public record PricingRequest(List<PricingTierRequest> tiers) {

}
