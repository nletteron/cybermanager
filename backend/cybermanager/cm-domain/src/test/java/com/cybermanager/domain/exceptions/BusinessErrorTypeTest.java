package com.cybermanager.domain.exceptions;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class BusinessErrorTypeTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertEnumContract(BusinessErrorType.class);
    }

}
