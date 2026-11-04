package com.cybermanager.domain.valueobjects.users;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class PasswordHashTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(PasswordHash.class);
    }

}
