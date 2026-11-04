package com.cybermanager.auth.domain.valueobjects;

import com.cybermanager.auth.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UserStatusTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertEnumContract(UserStatus.class);
    }

}
