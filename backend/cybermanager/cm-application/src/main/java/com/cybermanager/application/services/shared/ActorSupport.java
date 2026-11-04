package com.cybermanager.application.services.shared;

import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.valueobjects.users.AppRole;

import java.util.Set;

public final class ActorSupport {

    private ActorSupport() {
    }

    public static void requireAdmin(Set<String> roles) {
        if (roles == null || roles.stream().noneMatch(role -> AppRole.ADMIN.name().equalsIgnoreCase(role))) {
            throw new BusinessException(BusinessErrorType.FORBIDDEN, BusinessErrorCode.ADMIN_ROLE_REQUIRED, "Administrator role is required");
        }
    }

}

