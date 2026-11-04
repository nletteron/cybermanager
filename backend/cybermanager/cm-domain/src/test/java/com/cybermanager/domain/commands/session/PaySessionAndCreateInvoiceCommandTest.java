package com.cybermanager.domain.commands.session;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class PaySessionAndCreateInvoiceCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(PaySessionAndCreateInvoiceCommand.class);
    }

}
