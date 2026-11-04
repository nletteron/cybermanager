package com.cybermanager.application.usecases.session;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class SearchSessionsOfDayUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(SearchSessionsOfDayUseCase.class);
    }

}
