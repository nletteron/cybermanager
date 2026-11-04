package com.cybermanager.application.views.database;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DatabaseDumpViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DatabaseDumpView.class);
    }

}
