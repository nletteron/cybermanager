package com.cybermanager.domain.exceptions;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public class DomainException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final BusinessErrorType type;
    private final BusinessErrorCode code;
    private final HashMap<String, Object> parameters;

    public DomainException(BusinessErrorType type, BusinessErrorCode code, String message) {
        this(type, code, message, Map.of());
    }

    public DomainException(BusinessErrorType type, BusinessErrorCode code, String message, Map<String, Object> parameters) {
        super(message);
        this.type = type;
        this.code = code;
        this.parameters = new HashMap<>(parameters == null ? Map.of() : Map.copyOf(parameters));
    }

    public BusinessErrorType type() {
        return type;
    }

    public BusinessErrorCode businessErrorCode() {
        return code;
    }

    public String code() {
        return code.name();
    }

    public Map<String, Object> parameters() {
        return Map.copyOf(parameters);
    }

}
