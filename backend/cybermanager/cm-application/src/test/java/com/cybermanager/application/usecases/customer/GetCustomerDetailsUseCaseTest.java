package com.cybermanager.application.usecases.customer;

import com.cybermanager.application.ApplicationClassContractAssertions;
import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.customer.DebtRepository;
import com.cybermanager.domain.ports.customer.GeneratedArchiveFileRepository;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.ports.session.CafeSessionRepository;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.queries.customer.GetCustomerDetailsQuery;
import com.cybermanager.domain.valueobjects.sales.SaleLine;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import com.cybermanager.domain.valueobjects.shared.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetCustomerDetailsUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(GetCustomerDetailsUseCase.class);
    }

    @Test
    void shouldIdentifyDebtBySaleIdWhenLabelsAmountsAndTimesAreIdentical() {
        var customers = mock(CustomerRepository.class);
        var sales = mock(SaleRepository.class);
        var debts = mock(DebtRepository.class);
        var useCase = new GetCustomerDetailsUseCase(customers, mock(SubscriptionOfferRepository.class), sales,
                debts, mock(GeneratedArchiveFileRepository.class), mock(CafeSessionRepository.class));
        var customer = Customer.createWalkIn("Alice");
        var time = LocalDateTime.parse("2026-11-02T10:00:00");
        var lines = List.of(new SaleLine("Produit 1", 1, Money.of("2"), Money.of("2")));
        var first = Sale.create(customer.id(), SaleType.PRODUCTS, time, lines, Money.of("2"));
        var second = Sale.create(customer.id(), SaleType.PRODUCTS, time, lines, Money.of("2"));
        var debt = DebtRecord.createForSale(customer.id(), "1 x Produit 1 du 02/11/2026 10:00", Money.of("2"), time, second.id().value());
        when(customers.findById(customer.id())).thenReturn(Optional.of(customer));
        when(sales.findByCustomerId(customer.id())).thenReturn(List.of(first, second));
        when(debts.findByCustomerId(customer.id())).thenReturn(List.of(debt));

        var details = useCase.execute(new GetCustomerDetailsQuery(customer.id().value()));

        assertEquals(2, details.purchases().size());
        assertFalse(details.purchases().getFirst().openDebt());
        assertNull(details.purchases().getFirst().debtId());
        assertTrue(details.purchases().get(1).openDebt());
        assertEquals(debt.id().value(), details.purchases().get(1).debtId());
        assertEquals(1, details.purchases().getFirst().lines().getFirst().quantity());
    }

    @Test
    void shouldExposeRecordedProductAndSubscriptionQuantitiesAndPrices() {
        var customers = mock(CustomerRepository.class);
        var sales = mock(SaleRepository.class);
        var debts = mock(DebtRepository.class);
        var useCase = new GetCustomerDetailsUseCase(customers, mock(SubscriptionOfferRepository.class), sales,
                debts, mock(GeneratedArchiveFileRepository.class), mock(CafeSessionRepository.class));
        var customer = Customer.createWalkIn("Alice");
        var product = Sale.create(customer.id(), SaleType.PRODUCTS, LocalDateTime.now(),
                List.of(new SaleLine("Produit", 3, Money.of("2"), Money.of("6"))), Money.of("6"));
        var subscription = Sale.create(customer.id(), SaleType.SUBSCRIPTION, LocalDateTime.now(),
                List.of(new SaleLine("Forfait", 2, Money.of("5"), Money.of("10"))), Money.of("10"));
        when(customers.findById(customer.id())).thenReturn(Optional.of(customer));
        when(sales.findByCustomerId(customer.id())).thenReturn(List.of(product, subscription));

        var result = useCase.execute(new GetCustomerDetailsQuery(customer.id().value()));

        assertEquals(3, result.purchases().getFirst().lines().getFirst().quantity());
        assertEquals(product.lines().getFirst().unitPrice().amount(), result.purchases().getFirst().lines().getFirst().unitPrice());
        assertEquals(2, result.purchases().get(1).lines().getFirst().quantity());
        assertEquals(subscription.totalAmount().amount(), result.purchases().get(1).lines().getFirst().totalPrice());
    }

}
