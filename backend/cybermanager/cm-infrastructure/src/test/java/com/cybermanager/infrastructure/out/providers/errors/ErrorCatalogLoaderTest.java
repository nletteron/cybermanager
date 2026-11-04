package com.cybermanager.infrastructure.out.providers.errors;

import com.cybermanager.infrastructure.out.adapters.persistences.errors.ErrorCatalogPersistenceAdapter;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogJpaEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.DefaultResourceLoader;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ErrorCatalogLoaderTest {

    @TempDir
    private Path tempDir;

    @Test
    void shouldLoadBusinessAndTechnicalErrorsFromJsonFile() throws Exception {
        Path file = tempDir.resolve("error.json");
        Files.writeString(file, """
                {
                  "business": [
                    {
                      "domainCode": "CUSTOMER_NOT_FOUND",
                      "code": "CM-BUS-0008",
                      "message": "Customer not found",
                      "httpStatus": 404,
                      "boundedContext": "customer",
                      "active": true
                    }
                  ],
                  "technical": [
                    {
                      "domainCode": "INTERNAL_ERROR",
                      "code": "CM-TECH-0005",
                      "message": "Unexpected server error",
                      "httpStatus": 500,
                      "boundedContext": "api",
                      "active": true
                    }
                  ]
                }
                """);
        ErrorCatalogPersistenceAdapter adapter = mock(ErrorCatalogPersistenceAdapter.class);
        ErrorCatalogCache cache = mock(ErrorCatalogCache.class);
        ErrorCatalogLoader loader = new ErrorCatalogLoader(adapter, cache, new ObjectMapper(), new DefaultResourceLoader(), file.toUri().toString());

        loader.reload();

        ArgumentCaptor<ErrorCatalogJpaEntity> captor = ArgumentCaptor.forClass(ErrorCatalogJpaEntity.class);
        verify(adapter, org.mockito.Mockito.times(2)).saveOrUpdate(captor.capture());
        verify(cache).refresh();
        assertEquals("CUSTOMER_NOT_FOUND", captor.getAllValues().get(0).getDomainCode());
        assertEquals("INTERNAL_ERROR", captor.getAllValues().get(1).getDomainCode());
    }

}
