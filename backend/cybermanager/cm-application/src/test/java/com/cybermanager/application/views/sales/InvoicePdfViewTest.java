package com.cybermanager.application.views.sales;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class InvoicePdfViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(InvoicePdfView.class);
    }

}
