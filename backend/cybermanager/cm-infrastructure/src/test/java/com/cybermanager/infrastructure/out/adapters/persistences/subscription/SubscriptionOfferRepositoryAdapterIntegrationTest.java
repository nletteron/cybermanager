package com.cybermanager.infrastructure.out.adapters.persistences.subscription;

import com.cybermanager.domain.businessobjects.subscription.SubscriptionOffer;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferId;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferStatus;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionOfferRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private SubscriptionOfferRepositoryAdapter adapter;

    @Test
    void shouldSaveFindAndFilterSubscriptionOffersByStatus() {
        SubscriptionOffer active = adapter.save(SubscriptionOffer.create("Forfait 2h", Money.of("7.50"), 120));
        adapter.save(SubscriptionOffer.create("Forfait pause", Money.of("3.00"), 30).deactivate());

        var result = adapter.search("forfait", SubscriptionOfferStatus.ACTIVE);

        assertEquals(1, result.size());
        assertEquals(active.id(), result.get(0).id());
        assertTrue(adapter.findById(active.id()).isPresent());
    }

    @Test
    void shouldReturnEmptyWhenOfferDoesNotExistOrSearchDoesNotMatch() {
        assertTrue(adapter.findById(SubscriptionOfferId.newId()).isEmpty());
        assertTrue(adapter.search("missing", null).isEmpty());
    }

}
