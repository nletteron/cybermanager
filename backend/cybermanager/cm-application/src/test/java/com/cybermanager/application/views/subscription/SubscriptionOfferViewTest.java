package com.cybermanager.application.views.subscription;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SubscriptionOfferViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SubscriptionOfferView.class);
    }

}
