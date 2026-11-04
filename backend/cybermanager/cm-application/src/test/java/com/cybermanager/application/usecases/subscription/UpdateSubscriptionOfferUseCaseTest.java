package com.cybermanager.application.usecases.subscription;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class UpdateSubscriptionOfferUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(UpdateSubscriptionOfferUseCase.class);
    }

}
