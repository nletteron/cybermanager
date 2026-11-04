package com.cybermanager.infrastructure.out.providers.errors;

import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.exceptions.DomainException;
import com.cybermanager.infrastructure.abstracts.errors.ErrorDescriptor;
import com.cybermanager.infrastructure.abstracts.errors.resolvers.ErrorMessageDomainResolver;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogJpaEntity;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogTypeJpa;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DatabaseDomainErrorMessageResolver implements ErrorMessageDomainResolver {

    private static final Pattern TOKEN = Pattern.compile("\\{([a-zA-Z0-9_\\-.]+)\\}");
    private static final int DEFAULT_BAD_REQUEST_STATUS = 400;
    private static final int DEFAULT_FORBIDDEN_STATUS = 403;
    private static final int DEFAULT_NOT_FOUND_STATUS = 404;
    private static final int DEFAULT_CONFLICT_STATUS = 409;

    private final ErrorCatalogCache cache;

    public DatabaseDomainErrorMessageResolver(ErrorCatalogCache cache) {
        this.cache = cache;
    }

    @Override
    public ErrorDescriptor resolve(DomainException exception) {
        return resolveByCode(
                exception.code(),
                exception.parameters(),
                exception.getMessage(),
                toStatus(exception.type())
        );
    }

    @Override
    public ErrorDescriptor resolve(BusinessErrorCode domainCode, Map<String, Object> parameters) {
        return resolveByCode(domainCode.name(), parameters, domainCode.name(), DEFAULT_CONFLICT_STATUS);
    }

    private ErrorDescriptor resolveByCode(
            String domainCode,
            Map<String, Object> parameters,
            String defaultMessage,
            int defaultHttpStatus
    ) {
        ErrorDescriptor descriptor;
        var entry = cache.findByDomainCode(domainCode);

        if (entry.isPresent()) {
            descriptor = toDescriptor(entry.get(), parameters);
        } else {
            descriptor = fallbackDescriptor(domainCode, parameters, defaultMessage, defaultHttpStatus);
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
            int defaultHttpStatus
    ) {
        String message = renderTemplate(defaultMessage, parameters == null ? Map.of() : parameters);
        return new ErrorDescriptor(
                domainCode,
                domainCode,
                message,
                defaultHttpStatus,
                ErrorCatalogTypeJpa.BUSINESS.name(),
                "unknown",
                Instant.now()
        );
    }

    private int toStatus(BusinessErrorType type) {
        int status = switch (type) {
            case VALIDATION -> DEFAULT_BAD_REQUEST_STATUS;
            case FORBIDDEN -> DEFAULT_FORBIDDEN_STATUS;
            case NOT_FOUND -> DEFAULT_NOT_FOUND_STATUS;
            case CONFLICT -> DEFAULT_CONFLICT_STATUS;
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
