package com.cybermanager.domain.businessobjects.catalog;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(Product.class);
    }

}
