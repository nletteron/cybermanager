package com.cybermanager.api.dtos.users;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UserResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(UserResponse.class);
    }

}
