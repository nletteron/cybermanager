package com.cybermanager.infrastructure.out.providers.errors;

import java.util.List;

public record ErrorCatalogJsonFile(
        List<ErrorCatalogJsonEntry> business,
        List<ErrorCatalogJsonEntry> technical
) {

}
