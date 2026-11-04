package com.cybermanager.auth.domain.valueobjects;

import com.cybermanager.auth.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class AppRoleTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertEnumContract(AppRole.class);
    }

}
