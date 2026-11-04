package com.cybermanager.domain.commands.subscription;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class UpdateSubscriptionOfferCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(UpdateSubscriptionOfferCommand.class);
    }

}
