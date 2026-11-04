package com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales;

import com.cybermanager.infrastructure.out.entities.sales.ConnectionPricingTierJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionPricingJpaRepository extends JpaRepository<ConnectionPricingTierJpaEntity, Long> {

    List<ConnectionPricingTierJpaEntity> findAllByGridIdOrderByDurationMinutesAsc(Long gridId);

}

