package com.cybermanager.api.dtos.catalog;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ProductRequestTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ProductRequest.class);
    }

}
