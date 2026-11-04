package com.cybermanager.application.views.sales;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class PricingTierViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(PricingTierView.class);
    }

}
