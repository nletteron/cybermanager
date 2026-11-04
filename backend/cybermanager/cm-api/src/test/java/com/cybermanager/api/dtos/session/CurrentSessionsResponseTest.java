package com.cybermanager.api.dtos.session;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CurrentSessionsResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CurrentSessionsResponse.class);
    }

}
