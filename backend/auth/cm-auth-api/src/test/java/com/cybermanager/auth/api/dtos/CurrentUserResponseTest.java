package com.cybermanager.auth.api.dtos;

import com.cybermanager.auth.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CurrentUserResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CurrentUserResponse.class);
    }

}
