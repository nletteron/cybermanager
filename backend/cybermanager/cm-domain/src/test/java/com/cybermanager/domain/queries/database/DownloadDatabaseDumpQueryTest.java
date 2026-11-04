package com.cybermanager.domain.queries.database;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DownloadDatabaseDumpQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DownloadDatabaseDumpQuery.class);
    }

}
