package com.cybermanager.api.dtos.session;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class RestartSessionsDayResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(RestartSessionsDayResponse.class);
    }

}
