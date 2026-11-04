package com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer;

import com.cybermanager.infrastructure.out.entities.customer.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    List<CustomerJpaEntity> findAllByOrderByNameAsc();

    List<CustomerJpaEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<CustomerJpaEntity> findByTypeOrderByNameAsc(String type);

    List<CustomerJpaEntity> findByTypeAndNameContainingIgnoreCaseOrderByNameAsc(String type, String name);

}

