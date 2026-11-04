package com.cybermanager.infrastructure.out.adapters.persistences.repositories.subscription;

import com.cybermanager.infrastructure.out.entities.subscription.SubscriptionOfferJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionOfferJpaRepository extends JpaRepository<SubscriptionOfferJpaEntity, UUID> {

}

