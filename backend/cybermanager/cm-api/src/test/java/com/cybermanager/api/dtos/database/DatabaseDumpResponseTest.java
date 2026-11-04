package com.cybermanager.api.dtos.database;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DatabaseDumpResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DatabaseDumpResponse.class);
    }

}
