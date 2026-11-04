package com.cybermanager.application.usecases.session;

import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.session.CafeSession;

record PaidSessionResult(
        Customer customer,
        CafeSession session,
        SessionFinancialSnapshot snapshot
) {

}

