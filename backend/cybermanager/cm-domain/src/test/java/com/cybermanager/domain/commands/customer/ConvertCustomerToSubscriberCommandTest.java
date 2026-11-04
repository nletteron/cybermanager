package com.cybermanager.domain.commands.customer;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ConvertCustomerToSubscriberCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ConvertCustomerToSubscriberCommand.class);
    }

}
