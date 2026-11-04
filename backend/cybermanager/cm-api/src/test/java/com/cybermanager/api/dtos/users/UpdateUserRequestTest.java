package com.cybermanager.api.dtos.users;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UpdateUserRequestTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(UpdateUserRequest.class);
    }

}
