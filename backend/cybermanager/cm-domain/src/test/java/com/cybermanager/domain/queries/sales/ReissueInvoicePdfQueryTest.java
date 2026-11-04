package com.cybermanager.domain.queries.sales;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ReissueInvoicePdfQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ReissueInvoicePdfQuery.class);
    }

}
