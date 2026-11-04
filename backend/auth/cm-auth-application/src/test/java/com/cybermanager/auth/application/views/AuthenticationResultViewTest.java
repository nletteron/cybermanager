package com.cybermanager.auth.application.views;

import com.cybermanager.auth.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class AuthenticationResultViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(AuthenticationResultView.class);
    }

}
