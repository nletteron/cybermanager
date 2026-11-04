package com.cybermanager.domain.commands.subscription;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class DeleteSubscriptionOfferCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(DeleteSubscriptionOfferCommand.class);
    }

}
