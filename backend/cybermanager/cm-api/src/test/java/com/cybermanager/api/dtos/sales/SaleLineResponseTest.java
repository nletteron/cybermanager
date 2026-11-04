package com.cybermanager.api.dtos.sales;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SaleLineResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SaleLineResponse.class);
    }

}
