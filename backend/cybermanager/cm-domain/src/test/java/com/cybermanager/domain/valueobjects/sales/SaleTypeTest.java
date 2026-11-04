package com.cybermanager.domain.valueobjects.sales;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SaleTypeTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertEnumContract(SaleType.class);
    }

}
