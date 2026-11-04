package com.cybermanager.domain.valueobjects.catalog;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ProductStatusTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertEnumContract(ProductStatus.class);
    }

}
