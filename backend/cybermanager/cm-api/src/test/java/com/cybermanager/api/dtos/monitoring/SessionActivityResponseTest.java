package com.cybermanager.api.dtos.monitoring;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SessionActivityResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SessionActivityResponse.class);
    }

}
