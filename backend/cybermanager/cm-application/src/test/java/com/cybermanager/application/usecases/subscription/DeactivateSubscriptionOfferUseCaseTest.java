package com.cybermanager.application.usecases.subscription;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class DeactivateSubscriptionOfferUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(DeactivateSubscriptionOfferUseCase.class);
    }

}
