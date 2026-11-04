package com.cybermanager.api.errors;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ApiErrorResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ApiErrorResponse.class);
    }

}
