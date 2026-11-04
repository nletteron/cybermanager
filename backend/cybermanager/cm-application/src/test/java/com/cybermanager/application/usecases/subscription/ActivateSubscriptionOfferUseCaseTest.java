package com.cybermanager.application.usecases.subscription;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class ActivateSubscriptionOfferUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(ActivateSubscriptionOfferUseCase.class);
    }

}
