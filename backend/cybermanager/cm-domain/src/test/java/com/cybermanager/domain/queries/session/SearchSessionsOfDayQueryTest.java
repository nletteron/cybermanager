package com.cybermanager.domain.queries.session;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class SearchSessionsOfDayQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(SearchSessionsOfDayQuery.class);
    }

}
