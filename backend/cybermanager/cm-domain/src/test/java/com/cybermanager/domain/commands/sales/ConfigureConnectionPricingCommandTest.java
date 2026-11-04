package com.cybermanager.domain.commands.sales;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ConfigureConnectionPricingCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ConfigureConnectionPricingCommand.class);
    }

}
