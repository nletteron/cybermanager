package com.cybermanager.domain.exceptions;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class BusinessErrorCodeTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertEnumContract(BusinessErrorCode.class);
    }

}
