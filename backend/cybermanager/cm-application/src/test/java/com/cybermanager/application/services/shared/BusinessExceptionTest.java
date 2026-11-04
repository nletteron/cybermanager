package com.cybermanager.application.services.shared;

import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    void shouldExposeBusinessErrorMetadata() {
        BusinessException result = new BusinessException(BusinessErrorType.CONFLICT, BusinessErrorCode.USER_EMAIL_EXISTS, "Message");

        assertEquals(BusinessErrorType.CONFLICT, result.type());
        assertEquals(BusinessErrorCode.USER_EMAIL_EXISTS, result.businessErrorCode());
        assertEquals("USER_EMAIL_EXISTS", result.code());
        assertEquals("Message", result.getMessage());
    }

    @Test
    void shouldExposeDomainErrorParameters() {
        BusinessException result = new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.USER_NOT_FOUND, "Message {id}", Map.of("id", "42"));

        assertEquals(Map.of("id", "42"), result.parameters());
    }

}
