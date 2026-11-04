package com.cybermanager.domain.queries.users;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SearchUsersQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SearchUsersQuery.class);
    }

}
