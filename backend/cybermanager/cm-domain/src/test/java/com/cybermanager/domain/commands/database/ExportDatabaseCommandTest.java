package com.cybermanager.domain.commands.database;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ExportDatabaseCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ExportDatabaseCommand.class);
    }

}
