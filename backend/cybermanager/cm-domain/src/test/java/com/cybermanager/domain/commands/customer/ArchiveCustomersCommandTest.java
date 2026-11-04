package com.cybermanager.domain.commands.customer;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ArchiveCustomersCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ArchiveCustomersCommand.class);
    }

}
