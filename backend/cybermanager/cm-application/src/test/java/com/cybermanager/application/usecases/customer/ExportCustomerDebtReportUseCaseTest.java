package com.cybermanager.application.usecases.customer;

import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.queries.customer.ExportCustomerDebtReportQuery;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.CustomerStatus;
import com.cybermanager.domain.valueobjects.customer.CustomerType;
import com.cybermanager.domain.valueobjects.shared.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ExportCustomerDebtReportUseCaseTest {

    @Test
    void shouldExportDebtReportPdf() {
        CustomerUseCaseTestSupport support = CustomerUseCaseTestSupport.create();
        ExportCustomerDebtReportUseCase useCase = support.exportCustomerDebtReportUseCase();
        Customer customer = new Customer(new CustomerId(UUID.randomUUID()), "Client dette", CustomerType.WALK_IN, CustomerStatus.ACTIVE, 0);
        LocalDateTime activity = LocalDateTime.of(2026, 2, 10, 16, 0);

        when(support.customerRepository.findById(customer.id())).thenReturn(Optional.of(customer));
        when(support.debtRepository.findOpenDebts()).thenReturn(List.of(
                DebtRecord.create(customer.id(), "1 x Cafe du 10/02/2026 16:00", Money.of("2.00"), activity)
        ));

        var result = useCase.execute(new ExportCustomerDebtReportQuery("admin@cm.local", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), ""));

        assertEquals("application/pdf", result.mediaType());
        assertTrue(result.content().length > 0);
        assertEquals("customer-open-debts-report.pdf", result.fileName());
    }

    @Test
    void shouldIncludeOpenDebtsEvenForCustomerWithActiveSession() {
        CustomerUseCaseTestSupport support = CustomerUseCaseTestSupport.create();
        ExportCustomerDebtReportUseCase useCase = support.exportCustomerDebtReportUseCase();
        Customer customer = new Customer(new CustomerId(UUID.randomUUID()), "Session active", CustomerType.WALK_IN, CustomerStatus.ACTIVE, 0);

        when(support.customerRepository.findById(customer.id())).thenReturn(Optional.of(customer));
        when(support.debtRepository.findOpenDebts()).thenReturn(List.of(
                DebtRecord.create(customer.id(), "Dette en cours de session", Money.of("9.00"), LocalDateTime.of(2026, 3, 14, 20, 0))
        ));

        var result = useCase.execute(new ExportCustomerDebtReportQuery("admin@cm.local", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), ""));

        assertTrue(result.content().length > 0);
    }

}
