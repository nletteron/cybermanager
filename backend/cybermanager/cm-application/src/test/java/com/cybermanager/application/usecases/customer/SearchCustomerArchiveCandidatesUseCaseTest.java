package com.cybermanager.application.usecases.customer;

import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.businessobjects.session.CafeSession;
import com.cybermanager.domain.queries.customer.SearchCustomerArchiveCandidatesQuery;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SearchCustomerArchiveCandidatesUseCaseTest {

    @Test
    void shouldListArchiveCandidatesUsingLatestActivityInPeriod() {
        CustomerUseCaseTestSupport support = CustomerUseCaseTestSupport.create();
        SearchCustomerArchiveCandidatesUseCase useCase = support.searchArchiveCandidatesUseCase();
        Customer archivedCustomer = new Customer(new CustomerId(UUID.randomUUID()), "Client archive", CustomerType.WALK_IN, CustomerStatus.ACTIVE, 0);
        Customer recentCustomer = new Customer(new CustomerId(UUID.randomUUID()), "Client recent", CustomerType.WALK_IN, CustomerStatus.ACTIVE, 0);
        Customer activeCustomer = new Customer(new CustomerId(UUID.randomUUID()), "Client actif", CustomerType.WALK_IN, CustomerStatus.ACTIVE, 0);
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        when(support.customerRepository.search("", CustomerType.WALK_IN)).thenReturn(List.of(archivedCustomer, recentCustomer, activeCustomer));
        when(support.saleRepository.findByCustomerIds(any())).thenReturn(List.of(
                Sale.create(archivedCustomer.id(), SaleType.PRODUCTS, LocalDateTime.of(2026, 1, 20, 10, 0), List.of(new SaleLine("Cafe", 1, Money.of("2.00"), Money.of("2.00"))), Money.of("2.00")),
                Sale.create(recentCustomer.id(), SaleType.PRODUCTS, LocalDateTime.of(2026, 2, 2, 10, 0), List.of(new SaleLine("Snack", 1, Money.of("3.00"), Money.of("3.00"))), Money.of("3.00"))
        ));
        when(support.debtRepository.findByCustomerIds(any())).thenReturn(List.of());
        when(support.sessionRepository.findByCustomerIds(any())).thenReturn(List.of(
                new CafeSession(new SessionId(UUID.randomUUID()), activeCustomer.id(), "PC-01", LocalDateTime.of(2026, 1, 15, 9, 0), null, null, false, 0, 0, Money.of("0"))
        ));

        var result = useCase.execute(new SearchCustomerArchiveCandidatesQuery(startDate, endDate, "WALK_IN"));

        assertEquals(1, result.size());
        assertEquals("Client archive", result.getFirst().name());
    }

    @Test
    void shouldExcludeSubscribersAndCustomersWithDebtOrRemainingMinutesFromArchiveCandidates() {
        CustomerUseCaseTestSupport support = CustomerUseCaseTestSupport.create();
        SearchCustomerArchiveCandidatesUseCase useCase = support.searchArchiveCandidatesUseCase();
        Customer walkInWithDebt = new Customer(new CustomerId(UUID.randomUUID()), "Dette", CustomerType.WALK_IN, CustomerStatus.ACTIVE, 0);
        Customer subscriber = new Customer(new CustomerId(UUID.randomUUID()), "Abonne", CustomerType.SUBSCRIBER, CustomerStatus.ACTIVE, 0);
        Customer walkInWithCredit = new Customer(new CustomerId(UUID.randomUUID()), "Credit", CustomerType.WALK_IN, CustomerStatus.ACTIVE, 20);
        LocalDateTime activity = LocalDateTime.of(2026, 1, 10, 12, 0);

        when(support.customerRepository.search("", CustomerType.WALK_IN)).thenReturn(List.of(walkInWithDebt, walkInWithCredit));
        when(support.saleRepository.findByCustomerIds(any())).thenReturn(List.of(
                Sale.create(walkInWithDebt.id(), SaleType.PRODUCTS, activity, List.of(new SaleLine("Cafe", 1, Money.of("2.00"), Money.of("2.00"))), Money.of("2.00")),
                Sale.create(walkInWithCredit.id(), SaleType.PRODUCTS, activity, List.of(new SaleLine("Cafe", 1, Money.of("2.00"), Money.of("2.00"))), Money.of("2.00"))
        ));
        when(support.debtRepository.findByCustomerIds(any())).thenReturn(List.of(
                DebtRecord.create(walkInWithDebt.id(), "Ancienne dette", Money.of("5.00"), activity)
        ));
        when(support.sessionRepository.findByCustomerIds(any())).thenReturn(List.of());

        var result = useCase.execute(new SearchCustomerArchiveCandidatesQuery(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "WALK_IN"));

        assertEquals(0, result.size());

        when(support.customerRepository.search("", CustomerType.SUBSCRIBER)).thenReturn(List.of(subscriber));
        when(support.saleRepository.findByCustomerIds(any())).thenReturn(List.of(
                Sale.create(subscriber.id(), SaleType.SUBSCRIPTION, activity, List.of(new SaleLine("Forfait", 1, Money.of("10.00"), Money.of("10.00"))), Money.of("10.00"))
        ));
        when(support.debtRepository.findByCustomerIds(any())).thenReturn(List.of());

        var subscriberResult = useCase.execute(new SearchCustomerArchiveCandidatesQuery(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "SUBSCRIBER"));

        assertEquals(0, subscriberResult.size());
    }

}
