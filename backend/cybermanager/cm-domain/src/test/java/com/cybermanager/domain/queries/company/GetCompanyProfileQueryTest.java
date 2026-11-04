package com.cybermanager.domain.queries.company;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class GetCompanyProfileQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(GetCompanyProfileQuery.class);
    }

}
