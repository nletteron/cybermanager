package com.cybermanager.domain.ports.sales;

import com.cybermanager.domain.businessobjects.sales.ConnectionPricingRule;

public interface ConnectionPricingRepository {

    ConnectionPricingRule save(ConnectionPricingRule rule);

    ConnectionPricingRule getCurrentRule();

}

