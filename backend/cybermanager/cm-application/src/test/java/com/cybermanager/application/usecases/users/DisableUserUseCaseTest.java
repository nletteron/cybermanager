package com.cybermanager.application.usecases.users;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class DisableUserUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(DisableUserUseCase.class);
    }

}
