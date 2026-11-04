package com.cybermanager.api.dtos.monitoring;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DayCustomerActivityResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DayCustomerActivityResponse.class);
    }

}
