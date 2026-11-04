package com.cybermanager.domain.queries.customer;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class GetCustomerDetailsQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(GetCustomerDetailsQuery.class);
    }

}
