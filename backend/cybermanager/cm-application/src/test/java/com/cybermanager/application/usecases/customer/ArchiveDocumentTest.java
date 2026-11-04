package com.cybermanager.application.usecases.customer;

import com.cybermanager.application.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ArchiveDocumentTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ArchiveDocument.class);
    }

}
