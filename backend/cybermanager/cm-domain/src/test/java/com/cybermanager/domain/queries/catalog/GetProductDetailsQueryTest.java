package com.cybermanager.domain.queries.catalog;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class GetProductDetailsQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(GetProductDetailsQuery.class);
    }

}
