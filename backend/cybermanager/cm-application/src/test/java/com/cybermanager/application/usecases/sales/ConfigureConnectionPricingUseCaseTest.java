package com.cybermanager.application.usecases.sales;

import com.cybermanager.domain.businessobjects.sales.ConnectionPricingRule;
import com.cybermanager.domain.commands.sales.ConfigureConnectionPricingCommand;
import com.cybermanager.domain.commands.sales.PricingTierCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ConfigureConnectionPricingUseCaseTest {

    @Test
    void shouldExposeSortedPricingGrid() {
        SalesUseCaseTestSupport support = SalesUseCaseTestSupport.create();
        ConfigureConnectionPricingUseCase useCase = support.configureConnectionPricingUseCase();

        when(support.pricingRepository.save(any(ConnectionPricingRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new ConfigureConnectionPricingCommand(
                "admin@cybermanager.local",
                Set.of("ADMIN"),
                List.of(
                        new PricingTierCommand(1, 0, new BigDecimal("2.50")),
                        new PricingTierCommand(0, 30, new BigDecimal("1.50"))
                )
        ));

        assertEquals(2, result.tiers().size());
        assertEquals(30, result.tiers().get(0).durationMinutes());
        assertEquals(new BigDecimal("1.50"), result.tiers().get(0).price());
        assertEquals(60, result.tiers().get(1).durationMinutes());
    }

}
