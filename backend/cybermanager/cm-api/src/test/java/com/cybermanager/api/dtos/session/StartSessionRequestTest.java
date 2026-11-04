package com.cybermanager.api.dtos.session;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class StartSessionRequestTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(StartSessionRequest.class);
    }

}
