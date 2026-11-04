package com.cybermanager.infrastructure.out.providers.errors;

import com.cybermanager.infrastructure.out.adapters.persistences.errors.ErrorCatalogPersistenceAdapter;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ErrorCatalogCache {

    private final ErrorCatalogPersistenceAdapter persistenceAdapter;
    private final Map<String, ErrorCatalogJpaEntity> entriesByDomainCode;

    public ErrorCatalogCache(ErrorCatalogPersistenceAdapter persistenceAdapter) {
        this.persistenceAdapter = persistenceAdapter;
        this.entriesByDomainCode = new ConcurrentHashMap<>();
    }

    public synchronized void refresh() {
        entriesByDomainCode.clear();
        persistenceAdapter.findActiveErrors()
                .forEach(entry -> entriesByDomainCode.put(entry.getDomainCode(), entry));
    }

    public Optional<ErrorCatalogJpaEntity> findByDomainCode(String domainCode) {
        Optional<ErrorCatalogJpaEntity> result = Optional.ofNullable(entriesByDomainCode.get(domainCode));

        if (result.isEmpty()) {
            result = persistenceAdapter.findActiveByDomainCode(domainCode);
            result.ifPresent(entry -> entriesByDomainCode.put(entry.getDomainCode(), entry));
        }

        return result;
    }

}
