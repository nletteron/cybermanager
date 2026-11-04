package com.cybermanager.domain.businessobjects.sales;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ConnectionPricingRuleTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ConnectionPricingRule.class);
    }

}
