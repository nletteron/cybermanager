package com.cybermanager.domain.businessobjects.session;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class CafeSessionTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(CafeSession.class);
    }

}
