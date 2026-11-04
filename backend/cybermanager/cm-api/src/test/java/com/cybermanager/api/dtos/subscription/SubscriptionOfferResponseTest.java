package com.cybermanager.api.dtos.subscription;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SubscriptionOfferResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SubscriptionOfferResponse.class);
    }

}
