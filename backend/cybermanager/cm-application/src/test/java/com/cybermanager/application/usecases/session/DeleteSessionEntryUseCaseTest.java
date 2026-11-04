package com.cybermanager.application.usecases.session;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.businessobjects.session.CafeSession;
import com.cybermanager.domain.businessobjects.subscription.SubscriptionOffer;
import com.cybermanager.domain.commands.session.DeleteSessionEntryCommand;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.customer.DebtRepository;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.ports.session.CafeSessionRepository;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.SaleLine;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferId;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class DeleteSessionEntryUseCaseTest {

    private final CafeSessionRepository sessions = mock(CafeSessionRepository.class);
    private final SaleRepository sales = mock(SaleRepository.class);
    private final DebtRepository debts = mock(DebtRepository.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final SubscriptionOfferRepository offers = mock(SubscriptionOfferRepository.class);
    private final DeleteSessionEntryUseCase useCase = new DeleteSessionEntryUseCase(sessions, sales, debts, customers, offers);
    private final Customer customer = Customer.createWalkIn("Alice");
    private final CafeSession session = CafeSession.start(customer.id(), "SESSION", LocalDateTime.now().minusHours(1));
    private final Sale firstSale = productSale();
    private final Sale secondSale = productSale();
    private final DebtRecord firstDebt = DebtRecord.createForSale(customer.id(), "Produit 1", Money.of("2"), LocalDateTime.now(), firstSale.id().value());
    private final DebtRecord secondDebt = DebtRecord.createForSale(customer.id(), "Produit 1", Money.of("2"), LocalDateTime.now(), secondSale.id().value());

    @BeforeEach
    void configureRepositories() {
        when(sessions.findById(session.id())).thenReturn(Optional.of(session));
        when(sales.findById(firstSale.id())).thenReturn(Optional.of(firstSale));
        when(sales.findById(secondSale.id())).thenReturn(Optional.of(secondSale));
        when(debts.findById(firstDebt.id())).thenReturn(Optional.of(firstDebt));
        when(debts.findById(secondDebt.id())).thenReturn(Optional.of(secondDebt));
        when(debts.findByCustomerId(customer.id())).thenReturn(List.of(firstDebt, secondDebt));
    }

    @Test
    void shouldDeleteOnlySelectedPurchaseAndItsOwnDebt() {
        useCase.execute(new DeleteSessionEntryCommand(session.id().value(), firstSale.id().value(), null));

        verify(sales).deleteById(firstSale.id());
        verify(sales, never()).deleteById(secondSale.id());
        verify(debts).deleteById(firstDebt.id());
        verify(debts, never()).deleteById(secondDebt.id());
    }

    @Test
    void shouldDeleteOnlySelectedDebtAndItsOwnSale() {
        useCase.execute(new DeleteSessionEntryCommand(session.id().value(), null, secondDebt.id().value()));

        verify(debts).deleteById(secondDebt.id());
        verify(debts, never()).deleteById(firstDebt.id());
        verify(sales).deleteById(secondSale.id());
        verify(sales, never()).deleteById(firstSale.id());
    }

    @Test
    void shouldDeleteStandaloneDebtWithoutTouchingSales() {
        var debt = DebtRecord.create(customer.id(), "Session", Money.of("2"), LocalDateTime.now());
        when(debts.findById(debt.id())).thenReturn(Optional.of(debt));

        useCase.execute(new DeleteSessionEntryCommand(session.id().value(), null, debt.id().value()));

        verify(debts).deleteById(debt.id());
        verify(sales, never()).deleteById(any());
    }

    @Test
    void shouldRejectAnotherCustomersDebtWithoutDeletingAnything() {
        var debt = DebtRecord.create(CustomerId.newId(), "Produit 1", Money.of("2"), LocalDateTime.now());
        when(debts.findById(debt.id())).thenReturn(Optional.of(debt));

        assertThrows(BusinessException.class, () ->
                useCase.execute(new DeleteSessionEntryCommand(session.id().value(), null, debt.id().value())));

        verify(debts, never()).deleteById(any());
        verify(sales, never()).deleteById(any());
    }

    @Test
    void shouldRemoveCreditForTheExactSubscriptionQuantity() {
        var sale = Sale.create(customer.id(), session.id().value(), SaleType.SUBSCRIPTION, LocalDateTime.now(),
                List.of(new SaleLine("Forfait", 2, Money.of("3"), Money.of("6"))), Money.of("6"));
        when(sales.findById(sale.id())).thenReturn(Optional.of(sale));
        when(customers.findById(customer.id())).thenReturn(Optional.of(customer.addSubscriptionMinutes(180)));
        when(offers.search("Forfait", SubscriptionOfferStatus.ACTIVE)).thenReturn(List.of(
                new SubscriptionOffer(new SubscriptionOfferId(UUID.randomUUID()), "Forfait", Money.of("3"), 60, SubscriptionOfferStatus.ACTIVE)));

        useCase.execute(new DeleteSessionEntryCommand(session.id().value(), sale.id().value(), null));

        verify(customers).save(argThat(saved -> saved.remainingMinutes() == 60));
        verify(sales).deleteById(sale.id());
    }

    private Sale productSale() {
        return Sale.create(customer.id(), session.id().value(), SaleType.PRODUCTS, LocalDateTime.parse("2026-11-02T10:01:00"),
                List.of(new SaleLine("Produit 1", 1, Money.of("2"), Money.of("2"))), Money.of("2"));
    }

    @Test
    void shouldNotSilentlyKeepSubscriptionCreditWhenItsOfferIsMissing() {
        var sale = Sale.create(customer.id(), session.id().value(), SaleType.SUBSCRIPTION, LocalDateTime.now(),
                List.of(new SaleLine("Missing offer", 1, Money.of("3"), Money.of("3"))), Money.of("3"));
        when(sales.findById(sale.id())).thenReturn(Optional.of(sale));
        when(customers.findById(customer.id())).thenReturn(Optional.of(customer.addSubscriptionMinutes(60)));

        assertThrows(BusinessException.class, () ->
                useCase.execute(new DeleteSessionEntryCommand(session.id().value(), sale.id().value(), null)));

        verify(customers, never()).save(any());
        verify(sales, never()).deleteById(any());
    }

}
