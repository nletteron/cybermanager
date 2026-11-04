package com.cybermanager.domain.valueobjects.users;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class AppRoleTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertEnumContract(AppRole.class);
    }

}
