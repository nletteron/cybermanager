package com.cybermanager.application.usecases.subscription;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class GetSubscriptionOfferDetailsUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(GetSubscriptionOfferDetailsUseCase.class);
    }

}
