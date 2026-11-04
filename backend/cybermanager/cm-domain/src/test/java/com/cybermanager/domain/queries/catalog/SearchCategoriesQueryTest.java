package com.cybermanager.domain.queries.catalog;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SearchCategoriesQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SearchCategoriesQuery.class);
    }

}
