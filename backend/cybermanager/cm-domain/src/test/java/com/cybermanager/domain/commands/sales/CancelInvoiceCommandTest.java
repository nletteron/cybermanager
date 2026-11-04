package com.cybermanager.domain.commands.sales;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CancelInvoiceCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CancelInvoiceCommand.class);
    }

}
