package com.cybermanager.api.dtos.session;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SessionResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SessionResponse.class);
    }

}
