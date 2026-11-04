package com.cybermanager.application.usecases.catalog;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class SearchCategoriesUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(SearchCategoriesUseCase.class);
    }

}
