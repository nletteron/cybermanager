package com.cybermanager.auth.domain.queries;

import com.cybermanager.auth.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class LoadCurrentUserQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(LoadCurrentUserQuery.class);
    }

}
