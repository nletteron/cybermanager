package com.cybermanager.application.views.users;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UserSummaryViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(UserSummaryView.class);
    }

}
