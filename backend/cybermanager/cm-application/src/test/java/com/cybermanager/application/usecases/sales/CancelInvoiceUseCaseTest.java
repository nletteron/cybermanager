package com.cybermanager.application.usecases.sales;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class CancelInvoiceUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(CancelInvoiceUseCase.class);
    }

}
