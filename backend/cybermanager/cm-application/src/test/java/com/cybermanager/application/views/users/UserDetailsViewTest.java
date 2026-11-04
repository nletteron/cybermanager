package com.cybermanager.application.views.users;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UserDetailsViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(UserDetailsView.class);
    }

}
