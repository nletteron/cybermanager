package com.cybermanager.domain.businessobjects.company;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CompanyProfileTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CompanyProfile.class);
    }

}
