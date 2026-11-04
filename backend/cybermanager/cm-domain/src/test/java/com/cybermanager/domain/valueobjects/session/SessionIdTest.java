package com.cybermanager.domain.valueobjects.session;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SessionIdTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SessionId.class);
    }

}
