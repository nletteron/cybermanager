package com.cybermanager.domain.commands.session;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class StopSessionCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(StopSessionCommand.class);
    }

}
