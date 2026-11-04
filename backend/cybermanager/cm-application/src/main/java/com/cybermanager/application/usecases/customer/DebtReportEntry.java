package com.cybermanager.application.usecases.customer;

import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;

import java.math.BigDecimal;
import java.util.List;

record DebtReportEntry(Customer customer, List<DebtRecord> debts, BigDecimal total) {

}

