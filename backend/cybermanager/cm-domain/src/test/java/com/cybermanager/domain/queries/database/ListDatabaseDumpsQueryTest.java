package com.cybermanager.domain.queries.database;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ListDatabaseDumpsQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ListDatabaseDumpsQuery.class);
    }

}
