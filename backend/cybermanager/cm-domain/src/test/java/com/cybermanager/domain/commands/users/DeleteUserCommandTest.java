package com.cybermanager.domain.commands.users;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DeleteUserCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DeleteUserCommand.class);
    }

}
