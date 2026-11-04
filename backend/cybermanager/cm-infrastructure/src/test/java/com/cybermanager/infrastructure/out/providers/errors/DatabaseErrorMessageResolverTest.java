package com.cybermanager.infrastructure.out.providers.errors;

import com.cybermanager.infrastructure.abstracts.errors.InfrastructureErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseErrorMessageResolverTest {

    @Test
    void shouldFallbackToInfrastructureStatusWhenCatalogEntryIsMissing() {
        ErrorCatalogCache cache = mock(ErrorCatalogCache.class);
        when(cache.findByDomainCode("INVALID_TOKEN")).thenReturn(Optional.empty());
        DatabaseErrorMessageResolver resolver = new DatabaseErrorMessageResolver(cache);

        var result = resolver.resolve(InfrastructureErrorCode.INVALID_TOKEN);

        assertEquals("INVALID_TOKEN", result.code());
        assertEquals(401, result.httpStatus());
    }

}
