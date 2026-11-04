package com.cybermanager.domain.commands.session;

import com.cybermanager.domain.ContractTestSupport;
import org.junit.jupiter.api.Test;

class RestartSessionsDayCommandTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(RestartSessionsDayCommand.class);
    }

}
