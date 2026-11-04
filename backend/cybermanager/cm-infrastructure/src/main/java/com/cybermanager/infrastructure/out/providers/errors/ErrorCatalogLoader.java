package com.cybermanager.infrastructure.out.providers.errors;

import com.cybermanager.infrastructure.out.adapters.persistences.errors.ErrorCatalogPersistenceAdapter;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogJpaEntity;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogTypeJpa;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class ErrorCatalogLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorCatalogLoader.class);

    private final ErrorCatalogPersistenceAdapter persistenceAdapter;
    private final ErrorCatalogCache cache;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String errorCatalogFile;
    private long lastLoadedTimestamp;

    public ErrorCatalogLoader(
            ErrorCatalogPersistenceAdapter persistenceAdapter,
            ErrorCatalogCache cache,
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader,
            @Value("${cybermanager.errors.file:file:configuration/backends/cybermanager/error.json}") String errorCatalogFile
    ) {
        this.persistenceAdapter = persistenceAdapter;
        this.cache = cache;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.errorCatalogFile = errorCatalogFile;
        this.lastLoadedTimestamp = -1L;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadOnStartup() {
        reload();
    }

    @Scheduled(fixedDelayString = "${cybermanager.errors.reload-delay-ms:5000}")
    public void reloadIfChanged() {
        Resource resource = resolveResource();

        try {
            long timestamp = resource.lastModified();
            if (timestamp != lastLoadedTimestamp) {
                reload();
            }
        } catch (Exception exception) {
            LOGGER.warn("Unable to watch error catalog file={}", errorCatalogFile, exception);
        }
    }

    public synchronized void reload() {
        Resource resource = resolveResource();

        try (InputStream inputStream = resource.getInputStream()) {
            ErrorCatalogJsonFile jsonFile = objectMapper.readValue(inputStream, ErrorCatalogJsonFile.class);
            saveEntries(jsonFile.business(), ErrorCatalogTypeJpa.BUSINESS);
            saveEntries(jsonFile.technical(), ErrorCatalogTypeJpa.TECHNICAL);
            cache.refresh();
            lastLoadedTimestamp = resource.lastModified();
            LOGGER.info("Error catalog loaded from {}", errorCatalogFile);
        } catch (Exception exception) {
            LOGGER.error("Failed to load error catalog from {}", errorCatalogFile, exception);
        }
    }

    private void saveEntries(List<ErrorCatalogJsonEntry> entries, ErrorCatalogTypeJpa type) {
        if (entries != null) {
            entries.stream()
                    .map(entry -> toEntity(entry, type))
                    .forEach(persistenceAdapter::saveOrUpdate);
        }
    }

    private ErrorCatalogJpaEntity toEntity(ErrorCatalogJsonEntry entry, ErrorCatalogTypeJpa type) {
        ErrorCatalogJpaEntity entity = new ErrorCatalogJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setDomainCode(entry.domainCode());
        entity.setErrorCode(entry.code());
        entity.setType(type);
        entity.setMessage(entry.message());
        entity.setHttpStatus(entry.httpStatus());
        entity.setBoundedContext(entry.boundedContext());
        entity.setActive(entry.active() == null || entry.active());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    private Resource resolveResource() {
        List<String> candidates = List.of(
                errorCatalogFile,
                "file:configuration/backends/cybermanager/error.json",
                "file:../configuration/backends/cybermanager/error.json",
                "file:../../configuration/backends/cybermanager/error.json",
                "file:../../../configuration/backends/cybermanager/error.json"
        );
        Resource result = resourceLoader.getResource(errorCatalogFile);
        boolean found = false;

        for (String candidate : candidates) {
            Resource resource = resourceLoader.getResource(candidate);

            if (!found && exists(resource)) {
                result = resource;
                found = true;
            }
        }

        return result;
    }

    private boolean exists(Resource resource) {
        boolean result;

        try {
            result = resource.exists();
        } catch (Exception exception) {
            result = false;
        }

        return result;
    }

}
