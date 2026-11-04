package com.cybermanager.domain.commands.users;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ChangeUserStatusCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ChangeUserStatusCommand.class);
    }

}
