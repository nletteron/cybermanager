package com.cybermanager.application.usecases.sales;

import com.cybermanager.domain.businessobjects.sales.ConnectionPricingRule;
import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.commands.sales.CreateConnectionTimeSaleCommand;
import com.cybermanager.domain.valueobjects.sales.ConnectionPricingTier;
import com.cybermanager.domain.valueobjects.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CreateConnectionTimeSaleUseCaseTest {

    @Test
    void shouldCalculateConnectionSaleFromConfiguredPricingTiers() {
        SalesUseCaseTestSupport support = SalesUseCaseTestSupport.create();
        CreateConnectionTimeSaleUseCase useCase = support.createConnectionTimeSaleUseCase();
        ConnectionPricingRule rule = new ConnectionPricingRule(List.of(
                new ConnectionPricingTier(30, Money.of("1.50")),
                new ConnectionPricingTier(60, Money.of("2.50"))
        ));

        when(support.pricingRepository.getCurrentRule()).thenReturn(rule);
        when(support.saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new CreateConnectionTimeSaleCommand("admin@cybermanager.local", Set.of("ADMIN"), UUID.randomUUID(), null, 90, false));

        assertEquals(new BigDecimal("4.00"), result.totalAmount());
    }

}
