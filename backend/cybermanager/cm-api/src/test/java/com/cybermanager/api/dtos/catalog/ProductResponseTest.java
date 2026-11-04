package com.cybermanager.api.dtos.catalog;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ProductResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ProductResponse.class);
    }

}
