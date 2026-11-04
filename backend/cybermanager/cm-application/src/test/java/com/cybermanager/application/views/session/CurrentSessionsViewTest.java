package com.cybermanager.application.views.session;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CurrentSessionsViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CurrentSessionsView.class);
    }

}
