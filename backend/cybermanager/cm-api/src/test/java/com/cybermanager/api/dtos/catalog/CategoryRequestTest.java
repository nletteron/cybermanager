package com.cybermanager.api.dtos.catalog;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CategoryRequestTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CategoryRequest.class);
    }

}
