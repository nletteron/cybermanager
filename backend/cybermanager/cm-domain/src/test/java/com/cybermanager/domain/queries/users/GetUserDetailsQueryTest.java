package com.cybermanager.domain.queries.users;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class GetUserDetailsQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(GetUserDetailsQuery.class);
    }

}
