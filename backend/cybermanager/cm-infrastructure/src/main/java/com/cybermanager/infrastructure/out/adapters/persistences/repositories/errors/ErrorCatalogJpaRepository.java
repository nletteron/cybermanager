package com.cybermanager.infrastructure.out.adapters.persistences.repositories.errors;

import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ErrorCatalogJpaRepository extends JpaRepository<ErrorCatalogJpaEntity, UUID> {

    List<ErrorCatalogJpaEntity> findByActiveTrue();

    Optional<ErrorCatalogJpaEntity> findByDomainCodeAndActiveTrue(String domainCode);

    Optional<ErrorCatalogJpaEntity> findByDomainCode(String domainCode);

}
