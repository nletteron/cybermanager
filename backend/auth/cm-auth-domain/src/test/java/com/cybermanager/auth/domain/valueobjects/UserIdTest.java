package com.cybermanager.auth.domain.valueobjects;

import com.cybermanager.auth.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UserIdTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(UserId.class);
    }

}
