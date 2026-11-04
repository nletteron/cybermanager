package com.cybermanager.infrastructure.abstracts.errors;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public class InfrastructureException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final InfrastructureErrorCode code;
    private final HashMap<String, Object> parameters;

    public InfrastructureException(InfrastructureErrorCode code, String message) {
        this(code, message, Map.of());
    }

    public InfrastructureException(InfrastructureErrorCode code, String message, Map<String, Object> parameters) {
        super(message);
        this.code = code;
        this.parameters = new HashMap<>(parameters == null ? Map.of() : Map.copyOf(parameters));
    }

    public InfrastructureErrorCode code() {
        return code;
    }

    public Map<String, Object> parameters() {
        return Map.copyOf(parameters);
    }

}
