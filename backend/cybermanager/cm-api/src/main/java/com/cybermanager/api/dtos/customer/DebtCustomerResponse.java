package com.cybermanager.api.dtos.customer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DebtCustomerResponse(UUID customerId, String customerName, String customerType, BigDecimal totalOpenDebt, List<CustomerDebtResponse> debts) {

}
