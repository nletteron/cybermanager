package com.cybermanager.domain.valueobjects.customer;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DebtStatusTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertEnumContract(DebtStatus.class);
    }

}
