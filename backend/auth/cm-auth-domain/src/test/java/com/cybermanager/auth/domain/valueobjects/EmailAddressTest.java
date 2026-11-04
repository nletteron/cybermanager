package com.cybermanager.auth.domain.valueobjects;

import com.cybermanager.auth.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class EmailAddressTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(EmailAddress.class);
    }

}
