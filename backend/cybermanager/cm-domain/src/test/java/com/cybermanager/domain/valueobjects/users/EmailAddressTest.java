package com.cybermanager.domain.valueobjects.users;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class EmailAddressTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(EmailAddress.class);
    }

}
