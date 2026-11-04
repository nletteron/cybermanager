package com.cybermanager.api.dtos.company;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CompanyProfileRequestTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CompanyProfileRequest.class);
    }

}
