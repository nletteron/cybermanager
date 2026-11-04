package com.cybermanager.application.services.shared;

import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.exceptions.DomainException;

import java.io.Serial;
import java.util.Map;

public class BusinessException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BusinessException(BusinessErrorType type, BusinessErrorCode code, String message) {
        super(type, code, message);
    }

    public BusinessException(BusinessErrorType type, BusinessErrorCode code, String message, Map<String, Object> parameters) {
        super(type, code, message, parameters);
    }

}
