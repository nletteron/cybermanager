package com.cybermanager.infrastructure.out.providers.errors;

public record ErrorCatalogJsonEntry(
        String domainCode,
        String code,
        String message,
        int httpStatus,
        String boundedContext,
        Boolean active
) {

}
