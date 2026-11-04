package com.cybermanager.application.ports.database;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DumpContentTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DumpContent.class);
    }

}
