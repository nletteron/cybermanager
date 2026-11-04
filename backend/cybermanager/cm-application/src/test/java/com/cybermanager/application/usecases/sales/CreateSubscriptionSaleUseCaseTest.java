package com.cybermanager.application.usecases.sales;

import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.businessobjects.subscription.SubscriptionOffer;
import com.cybermanager.domain.commands.sales.CreateSubscriptionSaleCommand;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.CustomerStatus;
import com.cybermanager.domain.valueobjects.customer.CustomerType;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferId;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateSubscriptionSaleUseCaseTest {

    @Test
    void shouldCreateSubscriptionSaleWithQuantity() {
        SalesUseCaseTestSupport support = SalesUseCaseTestSupport.create();
        CreateSubscriptionSaleUseCase useCase = support.createSubscriptionSaleUseCase();
        UUID customerUuid = UUID.randomUUID();
        UUID offerUuid = UUID.randomUUID();
        Customer customer = new Customer(new CustomerId(customerUuid), "Yanis", CustomerType.SUBSCRIBER, CustomerStatus.ACTIVE, 60);
        SubscriptionOffer offer = new SubscriptionOffer(new SubscriptionOfferId(offerUuid), "Forfait 5h", Money.of("12.00"), 300, SubscriptionOfferStatus.ACTIVE);

        when(support.offerRepository.findById(new SubscriptionOfferId(offerUuid))).thenReturn(Optional.of(offer));
        when(support.customerRepository.findById(new CustomerId(customerUuid))).thenReturn(Optional.of(customer));
        when(support.customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(support.saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new CreateSubscriptionSaleCommand("admin@cybermanager.local", Set.of("ADMIN"), customerUuid, null, offerUuid, 2, false));

        assertEquals(new BigDecimal("24.00"), result.totalAmount());
        assertEquals(2, result.lines().getFirst().quantity());
        verify(support.customerRepository).save(argThat(saved -> saved.remainingMinutes() == 660));
    }

}
