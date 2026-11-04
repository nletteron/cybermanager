package com.cybermanager.domain.valueobjects.users;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UserStatusTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertEnumContract(UserStatus.class);
    }

}
