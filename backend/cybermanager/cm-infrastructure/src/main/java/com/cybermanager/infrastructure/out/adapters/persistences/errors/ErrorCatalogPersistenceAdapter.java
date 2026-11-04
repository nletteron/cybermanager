package com.cybermanager.infrastructure.out.adapters.persistences.errors;

import com.cybermanager.infrastructure.out.adapters.persistences.repositories.errors.ErrorCatalogJpaRepository;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ErrorCatalogPersistenceAdapter {

    private final ErrorCatalogJpaRepository repository;

    public ErrorCatalogPersistenceAdapter(ErrorCatalogJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ErrorCatalogJpaEntity> findActiveErrors() {
        return repository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Optional<ErrorCatalogJpaEntity> findActiveByDomainCode(String domainCode) {
        return repository.findByDomainCodeAndActiveTrue(domainCode);
    }

    @Transactional
    public ErrorCatalogJpaEntity saveOrUpdate(ErrorCatalogJpaEntity catalogEntry) {
        ErrorCatalogJpaEntity entity = repository.findByDomainCode(catalogEntry.getDomainCode())
                .orElseGet(ErrorCatalogJpaEntity::new);
        entity.setId(entity.getId() == null ? UUID.randomUUID() : entity.getId());
        entity.setDomainCode(catalogEntry.getDomainCode());
        entity.setErrorCode(catalogEntry.getErrorCode());
        entity.setType(catalogEntry.getType());
        entity.setMessage(catalogEntry.getMessage());
        entity.setHttpStatus(catalogEntry.getHttpStatus());
        entity.setBoundedContext(catalogEntry.getBoundedContext());
        entity.setActive(catalogEntry.isActive());
        entity.setUpdatedAt(LocalDateTime.now());
        return repository.save(entity);
    }

}
