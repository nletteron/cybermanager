package com.cybermanager.api.dtos.reporting;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SaleActivityResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SaleActivityResponse.class);
    }

}
