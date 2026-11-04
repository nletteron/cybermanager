package com.cybermanager.domain.commands.users;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CreateUserCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CreateUserCommand.class);
    }

}
