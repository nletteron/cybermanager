package com.cybermanager.api.dtos.session;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class PaySessionRequestTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(PaySessionRequest.class);
    }

}
