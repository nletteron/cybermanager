package com.cybermanager.domain.queries.subscription;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SearchSubscriptionOffersQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SearchSubscriptionOffersQuery.class);
    }

}
