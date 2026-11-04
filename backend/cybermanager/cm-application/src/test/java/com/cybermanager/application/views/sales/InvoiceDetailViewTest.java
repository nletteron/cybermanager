package com.cybermanager.application.views.sales;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class InvoiceDetailViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(InvoiceDetailView.class);
    }

}
