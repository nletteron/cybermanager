package com.cybermanager.application.views.reporting;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DayCustomerHistoryViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DayCustomerHistoryView.class);
    }

}
