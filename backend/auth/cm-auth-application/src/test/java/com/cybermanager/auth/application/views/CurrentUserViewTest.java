package com.cybermanager.auth.application.views;

import com.cybermanager.auth.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CurrentUserViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CurrentUserView.class);
    }

}
