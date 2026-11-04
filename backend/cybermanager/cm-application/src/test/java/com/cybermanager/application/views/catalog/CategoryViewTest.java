package com.cybermanager.application.views.catalog;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CategoryViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CategoryView.class);
    }

}
