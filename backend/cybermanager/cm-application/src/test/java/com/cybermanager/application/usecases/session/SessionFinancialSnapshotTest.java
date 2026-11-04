package com.cybermanager.application.usecases.session;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SessionFinancialSnapshotTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SessionFinancialSnapshot.class);
    }

}
