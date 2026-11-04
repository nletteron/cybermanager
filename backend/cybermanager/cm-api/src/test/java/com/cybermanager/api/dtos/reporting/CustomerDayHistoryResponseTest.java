package com.cybermanager.api.dtos.reporting;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CustomerDayHistoryResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CustomerDayHistoryResponse.class);
    }

}
