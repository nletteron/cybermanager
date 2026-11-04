package com.cybermanager.domain.queries.subscription;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class GetSubscriptionOfferDetailsQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(GetSubscriptionOfferDetailsQuery.class);
    }

}
