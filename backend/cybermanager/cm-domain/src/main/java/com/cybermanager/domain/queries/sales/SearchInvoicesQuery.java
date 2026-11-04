package com.cybermanager.domain.queries.sales;

import java.time.LocalDate;

public record SearchInvoicesQuery(String invoiceNumber, String customerName, String status, LocalDate startDate, LocalDate endDate) {

}
