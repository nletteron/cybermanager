package com.cybermanager.application.views.session;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SessionViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SessionView.class);
    }

}
