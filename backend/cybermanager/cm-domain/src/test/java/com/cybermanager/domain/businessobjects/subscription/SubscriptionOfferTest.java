package com.cybermanager.domain.businessobjects.subscription;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SubscriptionOfferTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SubscriptionOffer.class);
    }

}
