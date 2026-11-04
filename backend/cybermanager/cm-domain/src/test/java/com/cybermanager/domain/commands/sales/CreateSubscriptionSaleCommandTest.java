package com.cybermanager.domain.commands.sales;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CreateSubscriptionSaleCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CreateSubscriptionSaleCommand.class);
    }

}
