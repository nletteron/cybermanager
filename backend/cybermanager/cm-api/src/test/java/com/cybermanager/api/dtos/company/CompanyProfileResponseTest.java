package com.cybermanager.api.dtos.company;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CompanyProfileResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CompanyProfileResponse.class);
    }

}
