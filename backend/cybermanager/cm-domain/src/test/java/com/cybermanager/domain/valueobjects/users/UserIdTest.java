package com.cybermanager.domain.valueobjects.users;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UserIdTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(UserId.class);
    }

}
