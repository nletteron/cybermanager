package com.cybermanager.api.dtos.customer;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CustomerRequestTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CustomerRequest.class);
    }

}
