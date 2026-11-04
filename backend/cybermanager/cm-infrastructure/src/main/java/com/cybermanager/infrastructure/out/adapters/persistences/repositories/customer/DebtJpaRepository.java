package com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer;

import com.cybermanager.infrastructure.out.entities.customer.DebtJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DebtJpaRepository extends JpaRepository<DebtJpaEntity, UUID> {

    List<DebtJpaEntity> findByCustomerId(UUID customerId);

    List<DebtJpaEntity> findByCustomerIdIn(List<UUID> customerIds);

    List<DebtJpaEntity> findByStatus(String status);

    void deleteByCustomerIdIn(List<UUID> customerIds);

}
