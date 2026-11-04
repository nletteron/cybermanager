package com.cybermanager.application.services.shared;

import com.cybermanager.domain.exceptions.BusinessErrorType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActorSupportTest {

    @Test
    void shouldAllowAdminRoleIgnoringCase() {
        ActorSupport.requireAdmin(Set.of("admin"));
    }

    @Test
    void shouldRejectMissingAdminRole() {
        BusinessException exception = assertThrows(BusinessException.class, () -> ActorSupport.requireAdmin(Set.of("EMPLOYEE")));

        assertEquals(BusinessErrorType.FORBIDDEN, exception.type());
        assertEquals("ADMIN_ROLE_REQUIRED", exception.code());
    }

}
