package com.cybermanager.domain.ports.sales;

import com.cybermanager.domain.businessobjects.sales.Invoice;
import com.cybermanager.domain.valueobjects.sales.InvoiceStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {

    Invoice save(Invoice invoice);

    List<Invoice> search(String invoiceNumber, String customerName, InvoiceStatus status, LocalDate startDate, LocalDate endDate);

    Optional<Invoice> findById(UUID invoiceId);

}
