package com.cybermanager.application.usecases.users;

import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserManagementExceptionTest {

    @Test
    void shouldExposeBusinessErrorMetadata() {
        UserManagementException result = new UserManagementException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.USER_NOT_FOUND, "User not found");

        assertEquals(BusinessErrorType.NOT_FOUND, result.type());
        assertEquals("USER_NOT_FOUND", result.code());
        assertEquals("User not found", result.getMessage());
    }

}
