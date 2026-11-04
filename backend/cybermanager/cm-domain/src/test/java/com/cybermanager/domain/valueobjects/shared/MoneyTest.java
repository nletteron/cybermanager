package com.cybermanager.domain.valueobjects.shared;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(Money.class);
    }

}
