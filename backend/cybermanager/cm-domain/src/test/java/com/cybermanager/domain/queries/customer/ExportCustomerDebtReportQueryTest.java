package com.cybermanager.domain.queries.customer;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class ExportCustomerDebtReportQueryTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(ExportCustomerDebtReportQuery.class);
    }

}
