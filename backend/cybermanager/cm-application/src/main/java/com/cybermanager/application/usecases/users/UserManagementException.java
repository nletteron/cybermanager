package com.cybermanager.application.usecases.users;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;

import java.io.Serial;

public class UserManagementException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserManagementException(BusinessErrorType type, BusinessErrorCode code, String message) {
        super(type, code, message);
    }

}


