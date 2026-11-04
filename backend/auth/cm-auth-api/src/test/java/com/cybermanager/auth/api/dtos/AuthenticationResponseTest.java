package com.cybermanager.auth.api.dtos;

import com.cybermanager.auth.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class AuthenticationResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(AuthenticationResponse.class);
    }

}
