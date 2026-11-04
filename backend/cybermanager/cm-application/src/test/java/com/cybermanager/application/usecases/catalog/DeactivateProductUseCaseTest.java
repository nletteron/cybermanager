package com.cybermanager.application.usecases.catalog;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class DeactivateProductUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(DeactivateProductUseCase.class);
    }

}
