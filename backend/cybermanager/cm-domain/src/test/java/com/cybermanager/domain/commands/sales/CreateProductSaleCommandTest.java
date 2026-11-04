package com.cybermanager.domain.commands.sales;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CreateProductSaleCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CreateProductSaleCommand.class);
    }

}
