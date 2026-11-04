package com.cybermanager.domain.commands.session;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class StartSessionCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(StartSessionCommand.class);
    }

}
