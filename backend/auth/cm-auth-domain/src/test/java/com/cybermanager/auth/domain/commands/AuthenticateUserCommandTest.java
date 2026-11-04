package com.cybermanager.auth.domain.commands;

import com.cybermanager.auth.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class AuthenticateUserCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(AuthenticateUserCommand.class);
    }

}
