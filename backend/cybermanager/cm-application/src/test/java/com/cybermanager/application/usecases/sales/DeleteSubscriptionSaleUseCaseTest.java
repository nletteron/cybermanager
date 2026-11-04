package com.cybermanager.application.usecases.sales;

import com.cybermanager.application.services.shared.DateTimeLabelFormatter;
import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.businessobjects.session.CafeSession;
import com.cybermanager.domain.businessobjects.subscription.SubscriptionOffer;
import com.cybermanager.domain.valueobjects.customer.*;
import com.cybermanager.domain.valueobjects.sales.SaleId;
import com.cybermanager.domain.valueobjects.sales.SaleLine;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import com.cybermanager.domain.valueobjects.session.SessionId;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferId;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteSubscriptionSaleUseCaseTest {

    @Test
    void shouldDeleteSubscriptionSaleLinkedToAwaitingPaymentSession() {
        SalesUseCaseTestSupport support = SalesUseCaseTestSupport.create();
        DeleteSubscriptionSaleUseCase useCase = support.deleteSubscriptionSaleUseCase();
        UUID customerUuid = UUID.randomUUID();
        UUID sessionUuid = UUID.randomUUID();
        UUID saleUuid = UUID.randomUUID();
        Customer customer = new Customer(new CustomerId(customerUuid), "Nadia", CustomerType.SUBSCRIBER, CustomerStatus.ACTIVE, 120);
        Sale sale = new Sale(
                new SaleId(saleUuid),
                new CustomerId(customerUuid),
                sessionUuid,
                SaleType.SUBSCRIPTION,
                LocalDateTime.now().minusMinutes(5),
                List.of(new SaleLine("Forfait 2h", 1, Money.of("7.50"), Money.of("7.50"))),
                Money.of("7.50")
        );
        CafeSession session = new CafeSession(
                new SessionId(sessionUuid),
                new CustomerId(customerUuid),
                "SESSION",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusMinutes(2),
                null,
                false,
                0,
                3600,
                Money.of("4.00")
        );
        DebtRecord debt = new DebtRecord(
                DebtId.newId(),
                new CustomerId(customerUuid),
                "Vente abonnement du " + DateTimeLabelFormatter.format(sale.soldAt()),
                "",
                Money.of("7.50"),
                DebtStatus.OPEN,
                LocalDateTime.now().minusMinutes(4),
                null,
                saleUuid
        );

        when(support.saleRepository.findById(new SaleId(saleUuid))).thenReturn(Optional.of(sale));
        when(support.sessionRepository.findById(new SessionId(sessionUuid))).thenReturn(Optional.of(session));
        when(support.customerRepository.findById(new CustomerId(customerUuid))).thenReturn(Optional.of(customer));
        when(support.offerRepository.search("Forfait 2h", SubscriptionOfferStatus.ACTIVE)).thenReturn(List.of(
                new SubscriptionOffer(new SubscriptionOfferId(UUID.randomUUID()), "Forfait 2h", Money.of("7.50"), 120, SubscriptionOfferStatus.ACTIVE)
        ));
        when(support.debtRepository.findByCustomerId(new CustomerId(customerUuid))).thenReturn(List.of(debt));

        useCase.execute(saleUuid);

        verify(support.customerRepository).save(argThat(saved -> saved.remainingMinutes() == 0));
        verify(support.debtRepository).deleteById(debt.id());
        verify(support.saleRepository).deleteById(new SaleId(saleUuid));
    }

}
