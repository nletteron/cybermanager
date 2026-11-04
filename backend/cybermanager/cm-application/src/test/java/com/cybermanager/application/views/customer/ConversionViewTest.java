package com.cybermanager.application.views.customer;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ConversionViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ConversionView.class);
    }

}
