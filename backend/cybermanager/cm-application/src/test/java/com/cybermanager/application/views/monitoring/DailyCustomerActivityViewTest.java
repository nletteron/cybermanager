package com.cybermanager.application.views.monitoring;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DailyCustomerActivityViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DailyCustomerActivityView.class);
    }

}
