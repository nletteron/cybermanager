package com.cybermanager.application.usecases.session;

import com.cybermanager.application.ApplicationClassContractAssertions;
import org.junit.jupiter.api.Test;

class PaySessionAndCreateInvoiceUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(PaySessionAndCreateInvoiceUseCase.class);
    }

}
