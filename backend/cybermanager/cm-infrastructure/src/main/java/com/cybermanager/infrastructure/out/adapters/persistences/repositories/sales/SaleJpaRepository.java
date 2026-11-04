package com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales;

import com.cybermanager.infrastructure.out.entities.sales.SaleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SaleJpaRepository extends JpaRepository<SaleJpaEntity, UUID> {

    List<SaleJpaEntity> findBySoldAtBetween(LocalDateTime start, LocalDateTime end);

    List<SaleJpaEntity> findByCustomerIdOrderBySoldAtDesc(UUID customerId);

    List<SaleJpaEntity> findByCustomerIdInOrderBySoldAtDesc(List<UUID> customerIds);

    void deleteByCustomerIdIn(List<UUID> customerIds);

}
