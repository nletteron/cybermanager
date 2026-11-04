package com.cybermanager.auth.api.errors;

import com.cybermanager.auth.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ApiErrorResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ApiErrorResponse.class);
    }

}
