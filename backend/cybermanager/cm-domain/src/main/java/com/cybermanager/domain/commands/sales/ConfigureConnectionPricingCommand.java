package com.cybermanager.domain.commands.sales;

import java.util.List;
import java.util.Set;

public record ConfigureConnectionPricingCommand(
        String actorEmail,
        Set<String> actorRoles,
        List<PricingTierCommand> tiers
) {

}
