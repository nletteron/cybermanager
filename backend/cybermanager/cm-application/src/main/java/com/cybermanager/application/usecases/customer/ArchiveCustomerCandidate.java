package com.cybermanager.application.usecases.customer;

import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.businessobjects.session.CafeSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

record ArchiveCustomerCandidate(
        Customer customer,
        LocalDateTime latestActivityAt,
        List<CafeSession> sessions,
        List<Sale> sales,
        List<DebtRecord> debts,
        BigDecimal salesTotal,
        BigDecimal debtTotal
) {

}

