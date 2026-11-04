package com.cybermanager.domain.queries.customer;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SearchOpenDebtsQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SearchOpenDebtsQuery.class);
    }

}
