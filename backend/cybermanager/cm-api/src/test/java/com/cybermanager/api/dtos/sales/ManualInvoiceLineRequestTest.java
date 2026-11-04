package com.cybermanager.api.dtos.sales;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ManualInvoiceLineRequestTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ManualInvoiceLineRequest.class);
    }

}
