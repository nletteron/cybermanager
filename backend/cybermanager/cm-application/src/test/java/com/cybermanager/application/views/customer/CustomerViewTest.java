package com.cybermanager.application.views.customer;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CustomerViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CustomerView.class);
    }

}
