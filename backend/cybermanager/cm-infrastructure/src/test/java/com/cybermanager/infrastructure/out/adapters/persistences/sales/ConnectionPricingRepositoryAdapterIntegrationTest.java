package com.cybermanager.infrastructure.out.adapters.persistences.sales;

import com.cybermanager.domain.businessobjects.sales.ConnectionPricingRule;
import com.cybermanager.domain.valueobjects.sales.ConnectionPricingTier;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.ConnectionPricingGridJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionPricingRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private ConnectionPricingRepositoryAdapter adapter;
    @Autowired
    private ConnectionPricingGridJpaRepository gridRepository;

    @Test
    void shouldReturnEmptyRuleWhenNoGridExists() {
        assertTrue(adapter.getCurrentRule().tiers().isEmpty());
    }

    @Test
    void shouldSaveCurrentGridAndDeactivatePreviousGrid() {
        adapter.save(new ConnectionPricingRule(List.of(
                new ConnectionPricingTier(60, Money.of("4.00")),
                new ConnectionPricingTier(30, Money.of("2.00"))
        )));
        var secondRule = adapter.save(new ConnectionPricingRule(List.of(
                new ConnectionPricingTier(120, Money.of("7.50"))
        )));

        var current = adapter.getCurrentRule();

        assertEquals(1, current.tiers().size());
        assertEquals(120, current.tiers().get(0).durationMinutes());
        assertNotNull(secondRule.tiers().get(0).id());
        assertEquals(2, gridRepository.findAll().size());
        assertEquals(1, gridRepository.findAll().stream().filter(grid -> grid.active).count());
    }

}
