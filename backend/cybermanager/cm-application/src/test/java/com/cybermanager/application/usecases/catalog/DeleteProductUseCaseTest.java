package com.cybermanager.application.usecases.catalog;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class DeleteProductUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(DeleteProductUseCase.class);
    }

}
