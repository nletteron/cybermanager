package com.cybermanager.api.dtos.customer;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CustomerDebtResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CustomerDebtResponse.class);
    }

}
