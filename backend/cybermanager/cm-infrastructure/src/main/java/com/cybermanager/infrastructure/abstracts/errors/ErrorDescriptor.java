package com.cybermanager.infrastructure.abstracts.errors;

import java.time.Instant;

public record ErrorDescriptor(
        String domainCode,
        String code,
        String message,
        int httpStatus,
        String type,
        String boundedContext,
        Instant timestamp
) {

}
