package com.cybermanager.api.dtos.sales;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class InvoiceSummaryResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(InvoiceSummaryResponse.class);
    }

}
