package com.cybermanager.api.dtos.subscription;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SubscriptionOfferRequestTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SubscriptionOfferRequest.class);
    }

}
