package com.cybermanager.api.dtos.customer;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SettleDebtRequestTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SettleDebtRequest.class);
    }

}
