package com.cybermanager.domain.commands.catalog;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CreateProductCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CreateProductCommand.class);
    }

}
