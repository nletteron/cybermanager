package com.cybermanager.application.usecases.customer;

import com.cybermanager.application.ApplicationClassContractAssertions;
import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.commands.customer.CreateDebtFromSaleCommand;
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

class CreateDebtFromSaleUseCaseTest {

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(CreateDebtFromSaleUseCase.class);
    }

    @Test
    void shouldCreateDebtForSecondIdenticalSaleWithoutRejectingItAsDuplicate() {
        var sales = mock(SaleRepository.class);
        var debts = mock(DebtRepository.class);
        var useCase = new CreateDebtFromSaleUseCase(mock(CustomerRepository.class), mock(SubscriptionOfferRepository.class),
                sales, debts, mock(GeneratedArchiveFileRepository.class), mock(CafeSessionRepository.class));
        var customer = Customer.createWalkIn("Alice");
        var time = LocalDateTime.parse("2026-11-02T10:00:00");
        var lines = List.of(new SaleLine("Produit 1", 1, Money.of("2"), Money.of("2")));
        var first = Sale.create(customer.id(), SaleType.PRODUCTS, time, lines, Money.of("2"));
        var second = Sale.create(customer.id(), SaleType.PRODUCTS, time, lines, Money.of("2"));
        when(sales.findById(second.id())).thenReturn(Optional.of(second));
        when(debts.findByCustomerId(customer.id())).thenReturn(List.of(DebtRecord.createForSale(customer.id(),
                "1 x Produit 1 du 02/11/2026 10:00", Money.of("2"), time, first.id().value())));

        useCase.execute(new CreateDebtFromSaleCommand("staff", Set.of("EMPLOYEE"), second.id().value()));

        verify(debts).save(argThat(debt -> debt.isForSale(second.id()) && !debt.isForSale(first.id())));
    }

}
