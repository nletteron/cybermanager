package com.cybermanager.api.dtos.users;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CreateUserRequestTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CreateUserRequest.class);
    }

}
