package com.cybermanager.infrastructure.out.adapters.persistences.repositories.catalog;

import com.cybermanager.infrastructure.out.entities.catalog.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

}

