package com.cybermanager.api.dtos.monitoring;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DayCustomerResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DayCustomerResponse.class);
    }

}
