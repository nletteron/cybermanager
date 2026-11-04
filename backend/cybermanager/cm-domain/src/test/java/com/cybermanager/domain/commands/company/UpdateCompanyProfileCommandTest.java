package com.cybermanager.domain.commands.company;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UpdateCompanyProfileCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(UpdateCompanyProfileCommand.class);
    }

}
