package com.cybermanager.api.dtos.sales;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class InvoiceDetailResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(InvoiceDetailResponse.class);
    }

}
