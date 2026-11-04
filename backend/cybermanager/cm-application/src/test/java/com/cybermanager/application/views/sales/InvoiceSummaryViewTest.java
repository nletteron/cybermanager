package com.cybermanager.application.views.sales;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class InvoiceSummaryViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(InvoiceSummaryView.class);
    }

}
