package com.cybermanager.infrastructure.out.providers.errors;

import com.cybermanager.infrastructure.abstracts.errors.ErrorDescriptor;
import com.cybermanager.infrastructure.abstracts.errors.InfrastructureErrorCode;
import com.cybermanager.infrastructure.abstracts.errors.InfrastructureException;
import com.cybermanager.infrastructure.abstracts.errors.resolvers.ErrorMessageInfrastructureResolver;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogJpaEntity;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogTypeJpa;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DatabaseErrorMessageResolver implements ErrorMessageInfrastructureResolver {

    private static final Pattern TOKEN = Pattern.compile("\\{([a-zA-Z0-9_\\-.]+)\\}");
    private static final int DEFAULT_BAD_REQUEST_STATUS = 400;
    private static final int DEFAULT_NOT_FOUND_STATUS = 404;
    private static final int DEFAULT_SERVER_ERROR_STATUS = 500;

    private final ErrorCatalogCache cache;

    public DatabaseErrorMessageResolver(ErrorCatalogCache cache) {
        this.cache = cache;
    }

    @Override
    public ErrorDescriptor resolve(InfrastructureErrorCode code) {
        return resolve(code, Map.of());
    }

    @Override
    public ErrorDescriptor resolve(InfrastructureException exception) {
        return resolveByCode(exception.code().name(), exception.parameters(), exception.getMessage(), ErrorCatalogTypeJpa.TECHNICAL.name(), toStatus(exception.code()));
    }

    @Override
    public ErrorDescriptor resolve(InfrastructureErrorCode code, Map<String, Object> parameters) {
        return resolveByCode(code.name(), parameters, code.name(), ErrorCatalogTypeJpa.TECHNICAL.name(), toStatus(code));
    }

    private ErrorDescriptor resolveByCode(
            String domainCode,
            Map<String, Object> parameters,
            String defaultMessage,
            String defaultType,
            int defaultHttpStatus
    ) {
        ErrorDescriptor descriptor;
        var entry = cache.findByDomainCode(domainCode);

        if (entry.isPresent()) {
            descriptor = toDescriptor(entry.get(), parameters);
        } else {
            descriptor = fallbackDescriptor(domainCode, parameters, defaultMessage, defaultType, defaultHttpStatus);
        }

        return descriptor;
    }

    private ErrorDescriptor toDescriptor(ErrorCatalogJpaEntity entry, Map<String, Object> parameters) {
        Map<String, Object> safeParameters = parameters == null ? Map.of() : parameters;
        return new ErrorDescriptor(
                entry.getDomainCode(),
                entry.getErrorCode(),
                renderTemplate(entry.getMessage(), safeParameters),
                entry.getHttpStatus(),
                entry.getType().name(),
                renderTemplate(entry.getBoundedContext(), safeParameters),
                Instant.now()
        );
    }

    private ErrorDescriptor fallbackDescriptor(
            String domainCode,
            Map<String, Object> parameters,
            String defaultMessage,
            String defaultType,
            int defaultHttpStatus
    ) {
        String message = renderTemplate(defaultMessage, parameters == null ? Map.of() : parameters);
        String type = defaultType == null ? ErrorCatalogTypeJpa.BUSINESS.name() : defaultType;
        return new ErrorDescriptor(domainCode, domainCode, message, defaultHttpStatus, type, "unknown", Instant.now());
    }

    private int toStatus(InfrastructureErrorCode code) {
        int status = switch (code) {
            case BAD_REQUEST -> DEFAULT_BAD_REQUEST_STATUS;
            case INVALID_TOKEN -> 401;
            case ROUTE_NOT_FOUND -> DEFAULT_NOT_FOUND_STATUS;
            case METHOD_NOT_ALLOWED -> 405;
            case INTERNAL_ERROR, ERROR_CATALOG_LOAD_FAILED -> DEFAULT_SERVER_ERROR_STATUS;
        };
        return status;
    }

    private String renderTemplate(String template, Map<String, Object> parameters) {
        String result = "";

        if (template != null && !template.isBlank()) {
            Matcher matcher = TOKEN.matcher(template);

            if (!matcher.find()) {
                result = template;
            } else {
                StringBuilder builder = new StringBuilder(template.length() + 16);
                int last = 0;

                do {
                    builder.append(template, last, matcher.start());
                    String key = matcher.group(1);
                    Object value = parameters.get(key);
                    builder.append(value == null ? "{" + key + "}" : value);
                    last = matcher.end();
                } while (matcher.find());

                builder.append(template, last, template.length());
                result = builder.toString();
            }
        }

        return result;
    }

}
