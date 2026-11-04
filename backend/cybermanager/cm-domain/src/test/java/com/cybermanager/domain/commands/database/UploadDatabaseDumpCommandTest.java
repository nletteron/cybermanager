package com.cybermanager.domain.commands.database;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UploadDatabaseDumpCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(UploadDatabaseDumpCommand.class);
    }

}
