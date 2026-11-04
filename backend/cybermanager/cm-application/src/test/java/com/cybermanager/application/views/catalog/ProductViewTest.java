package com.cybermanager.application.views.catalog;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ProductViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ProductView.class);
    }

}
