package com.cybermanager.application.views.sales;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SaleLineViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SaleLineView.class);
    }

}
