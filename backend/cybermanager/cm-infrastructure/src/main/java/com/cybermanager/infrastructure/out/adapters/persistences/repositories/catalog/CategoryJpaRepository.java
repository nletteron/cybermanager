package com.cybermanager.infrastructure.out.adapters.persistences.repositories.catalog;

import com.cybermanager.infrastructure.out.entities.catalog.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    boolean existsByNameIgnoreCase(String name);

}
