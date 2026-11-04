package com.cybermanager.application.views.reporting;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CustomerDayHistoryViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CustomerDayHistoryView.class);
    }

}
