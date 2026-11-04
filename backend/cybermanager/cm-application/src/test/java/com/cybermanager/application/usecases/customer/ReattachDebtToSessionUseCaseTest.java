package com.cybermanager.application.usecases.customer;

import com.cybermanager.application.ApplicationClassContractAssertions;
import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.businessobjects.session.CafeSession;
import com.cybermanager.domain.commands.customer.ReattachDebtToSessionCommand;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.customer.DebtRepository;
import com.cybermanager.domain.ports.customer.GeneratedArchiveFileRepository;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.ports.session.CafeSessionRepository;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.valueobjects.sales.SaleLine;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import com.cybermanager.domain.valueobjects.shared.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class ReattachDebtToSessionUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(ReattachDebtToSessionUseCase.class);
    }

    @Test
    void shouldReattachOnlyTheSaleIdentifiedByTheSelectedDebt() {
        var sales = mock(SaleRepository.class);
        var debts = mock(DebtRepository.class);
        var sessions = mock(CafeSessionRepository.class);
        var useCase = new ReattachDebtToSessionUseCase(mock(CustomerRepository.class), mock(SubscriptionOfferRepository.class),
                sales, debts, mock(GeneratedArchiveFileRepository.class), sessions);
        var customer = Customer.createWalkIn("Alice");
        var time = LocalDateTime.parse("2026-11-02T10:00:00");
        var lines = List.of(new SaleLine("Produit 1", 1, Money.of("2"), Money.of("2")));
        var first = Sale.create(customer.id(), SaleType.PRODUCTS, time, lines, Money.of("2"));
        var second = Sale.create(customer.id(), SaleType.PRODUCTS, time, lines, Money.of("2"));
        var debt = DebtRecord.createForSale(customer.id(), "1 x Produit 1 du 02/11/2026 10:00", Money.of("2"), time, second.id().value());
        var session = CafeSession.start(customer.id(), "SESSION", time.plusHours(1));
        when(debts.findById(debt.id())).thenReturn(Optional.of(debt));
        when(sessions.findById(session.id())).thenReturn(Optional.of(session));
        when(sales.findById(second.id())).thenReturn(Optional.of(second));

        useCase.execute(new ReattachDebtToSessionCommand("staff", Set.of("EMPLOYEE"), debt.id().value(), session.id().value()));

        verify(sales).save(argThat(sale -> sale.id().equals(second.id()) && sale.sessionId().equals(session.id().value())));
        verify(sales, never()).save(argThat(sale -> sale.id().equals(first.id())));
        verify(debts).save(argThat(saved -> saved.id().equals(debt.id()) && saved.saleId().equals(second.id().value())));
    }

}
