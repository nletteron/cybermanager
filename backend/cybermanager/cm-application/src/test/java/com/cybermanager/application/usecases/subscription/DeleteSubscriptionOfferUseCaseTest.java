package com.cybermanager.application.usecases.subscription;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class DeleteSubscriptionOfferUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(DeleteSubscriptionOfferUseCase.class);
    }

}
