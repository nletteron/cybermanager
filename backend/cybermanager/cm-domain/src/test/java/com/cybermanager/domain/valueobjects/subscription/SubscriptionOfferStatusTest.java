package com.cybermanager.domain.valueobjects.subscription;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SubscriptionOfferStatusTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertEnumContract(SubscriptionOfferStatus.class);
    }

}
