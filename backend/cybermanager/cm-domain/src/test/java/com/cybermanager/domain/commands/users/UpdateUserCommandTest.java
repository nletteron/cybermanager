package com.cybermanager.domain.commands.users;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UpdateUserCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(UpdateUserCommand.class);
    }

}
