package com.cybermanager.application.usecases.customer;

import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.businessobjects.session.CafeSession;
import com.cybermanager.domain.commands.customer.ArchiveCustomersCommand;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.CustomerStatus;
import com.cybermanager.domain.valueobjects.customer.CustomerType;
import com.cybermanager.domain.valueobjects.sales.SaleLine;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import com.cybermanager.domain.valueobjects.session.SessionId;
import com.cybermanager.domain.valueobjects.shared.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class ArchiveCustomersUseCaseTest {

    @Test
    void shouldExportCsvAndDeleteArchivedCustomersData() {
        CustomerUseCaseTestSupport support = CustomerUseCaseTestSupport.create();
        ArchiveCustomersUseCase useCase = support.archiveCustomersUseCase();
        Customer archivedCustomer = new Customer(new CustomerId(UUID.randomUUID()), "Nadia", CustomerType.WALK_IN, CustomerStatus.ACTIVE, 0);
        LocalDateTime soldAt = LocalDateTime.of(2025, 12, 15, 14, 30);
        Sale sale = Sale.create(archivedCustomer.id(), SaleType.PRODUCTS, soldAt, List.of(new SaleLine("Casque", 1, Money.of("8.00"), Money.of("8.00"))), Money.of("8.00"));
        CafeSession session = new CafeSession(new SessionId(UUID.randomUUID()), archivedCustomer.id(), "PC-02", soldAt.minusHours(1), soldAt.minusMinutes(15), null, true, 0, 2700, Money.of("4.00"));

        when(support.customerRepository.search("", CustomerType.WALK_IN)).thenReturn(List.of(archivedCustomer));
        when(support.saleRepository.findByCustomerIds(any())).thenReturn(List.of(sale));
        when(support.debtRepository.findByCustomerIds(any())).thenReturn(List.of());
        when(support.sessionRepository.findByCustomerIds(any())).thenReturn(List.of(session));

        var result = useCase.execute(new ArchiveCustomersCommand("admin@cm.local", Set.of("ADMIN"), LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 31), "WALK_IN", "csv"));

        assertEquals(1, result.archivedCustomers());
        assertEquals("customers-archive-walk_in-2025-12-01-to-2025-12-31.csv", result.fileName());
        assertTrue(new String(result.content()).contains("Nadia"));
        verify(support.debtRepository).deleteByCustomerIds(argThat(ids -> ids.size() == 1 && ids.getFirst().equals(archivedCustomer.id())));
        verify(support.saleRepository).deleteByCustomerIds(argThat(ids -> ids.size() == 1 && ids.getFirst().equals(archivedCustomer.id())));
        verify(support.sessionRepository).deleteByCustomerIds(argThat(ids -> ids.size() == 1 && ids.getFirst().equals(archivedCustomer.id())));
        verify(support.customerRepository).deleteByIds(argThat(ids -> ids.size() == 1 && ids.getFirst().equals(archivedCustomer.id())));
    }

    @Test
    void shouldNotDeleteAnythingWhenArchiveMatchesNoCustomer() {
        CustomerUseCaseTestSupport support = CustomerUseCaseTestSupport.create();
        ArchiveCustomersUseCase useCase = support.archiveCustomersUseCase();

        when(support.customerRepository.search("", CustomerType.WALK_IN)).thenReturn(List.of());

        var result = useCase.execute(new ArchiveCustomersCommand("admin@cm.local", Set.of("ADMIN"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), "WALK_IN", "csv"));

        assertEquals(0, result.archivedCustomers());
        verify(support.debtRepository, never()).deleteByCustomerIds(any());
        verify(support.saleRepository, never()).deleteByCustomerIds(any());
        verify(support.sessionRepository, never()).deleteByCustomerIds(any());
        verify(support.customerRepository, never()).deleteByIds(any());
    }

    @Test
    void shouldExportXlsxArchive() {
        CustomerUseCaseTestSupport support = CustomerUseCaseTestSupport.create();
        ArchiveCustomersUseCase useCase = support.archiveCustomersUseCase();
        Customer archivedCustomer = new Customer(new CustomerId(UUID.randomUUID()), "Client xlsx", CustomerType.WALK_IN, CustomerStatus.ACTIVE, 0);
        LocalDateTime soldAt = LocalDateTime.of(2025, 11, 15, 14, 30);

        when(support.customerRepository.search("", CustomerType.WALK_IN)).thenReturn(List.of(archivedCustomer));
        when(support.saleRepository.findByCustomerIds(any())).thenReturn(List.of(
                Sale.create(archivedCustomer.id(), SaleType.PRODUCTS, soldAt, List.of(new SaleLine("Casque", 1, Money.of("8.00"), Money.of("8.00"))), Money.of("8.00"))
        ));
        when(support.debtRepository.findByCustomerIds(any())).thenReturn(List.of());
        when(support.sessionRepository.findByCustomerIds(any())).thenReturn(List.of());

        var result = useCase.execute(new ArchiveCustomersCommand("admin@cm.local", Set.of("ADMIN"), LocalDate.of(2025, 11, 1), LocalDate.of(2025, 11, 30), "WALK_IN", "xlsx"));

        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", result.mediaType());
        assertTrue(result.content().length > 0);
    }

}
