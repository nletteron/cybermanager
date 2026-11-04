package com.cybermanager.application.usecases.users;

import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.valueobjects.users.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsersApplicationSupportTest {

    @Test
    void shouldResolveBlankStatusAsNull() {
        assertNull(UsersApplicationSupport.status(" "));
    }

    @Test
    void shouldResolveStatusIgnoringCase() {
        assertEquals(UserStatus.ACTIVE, UsersApplicationSupport.status("active"));
    }

    @Test
    void shouldRejectNonAdminActor() {
        UserManagementException exception = assertThrows(UserManagementException.class, () -> UsersApplicationSupport.requireAdmin(Set.of("EMPLOYEE")));

        assertEquals(BusinessErrorType.FORBIDDEN, exception.type());
        assertEquals("ADMIN_ROLE_REQUIRED", exception.code());
    }

}
