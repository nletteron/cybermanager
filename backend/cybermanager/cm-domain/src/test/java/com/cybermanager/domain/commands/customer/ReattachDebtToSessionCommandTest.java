package com.cybermanager.domain.commands.customer;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ReattachDebtToSessionCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ReattachDebtToSessionCommand.class);
    }

}
