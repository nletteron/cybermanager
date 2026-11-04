package com.cybermanager.application.usecases.sales;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class GetInvoiceDetailUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(GetInvoiceDetailUseCase.class);
    }

}
