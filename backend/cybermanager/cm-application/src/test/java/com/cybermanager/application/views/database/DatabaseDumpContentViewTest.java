package com.cybermanager.application.views.database;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DatabaseDumpContentViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DatabaseDumpContentView.class);
    }

}
