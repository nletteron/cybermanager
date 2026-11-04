package com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales;

import com.cybermanager.infrastructure.out.entities.sales.ConnectionPricingGridJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConnectionPricingGridJpaRepository extends JpaRepository<ConnectionPricingGridJpaEntity, Long> {

    Optional<ConnectionPricingGridJpaEntity> findFirstByActiveTrue();

}
