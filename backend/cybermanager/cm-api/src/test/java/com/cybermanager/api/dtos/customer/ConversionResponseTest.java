package com.cybermanager.api.dtos.customer;

import com.cybermanager.api.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ConversionResponseTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ConversionResponse.class);
    }

}
