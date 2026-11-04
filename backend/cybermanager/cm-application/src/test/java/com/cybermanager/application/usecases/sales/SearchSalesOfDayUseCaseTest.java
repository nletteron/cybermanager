package com.cybermanager.application.usecases.sales;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class SearchSalesOfDayUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(SearchSalesOfDayUseCase.class);
    }

}
