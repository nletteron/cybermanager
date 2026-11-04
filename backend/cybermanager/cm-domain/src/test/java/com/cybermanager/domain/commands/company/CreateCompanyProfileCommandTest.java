package com.cybermanager.domain.commands.company;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CreateCompanyProfileCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CreateCompanyProfileCommand.class);
    }

}
