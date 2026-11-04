package com.cybermanager.api.dtos.sales;

import java.util.List;

public record PricingResponse(List<PricingTierResponse> tiers) {

}
