package com.cybermanager.application.usecases.session;

import com.cybermanager.domain.valueobjects.shared.Money;

import java.math.BigDecimal;

record SessionFinancialSnapshot(
        Money connectionAmount,
        Money purchasesAmount,
        Money openDebtAmount,
        BigDecimal payablePurchasesAmount,
        Money totalAmountDue,
        Money totalPaidAmount
) {

}

