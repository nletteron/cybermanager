package com.cybermanager.application.views.company;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CompanyProfileViewTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CompanyProfileView.class);
    }

}
