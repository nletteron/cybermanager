package com.cybermanager.application.usecases.users;

import com.cybermanager.domain.businessobjects.users.User;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.valueobjects.users.*;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

final class UsersApplicationSupport {

    private UsersApplicationSupport() {
    }

    static void requireAdmin(Set<String> roles) {
        if (roles == null || roles.stream().noneMatch(role -> AppRole.ADMIN.name().equalsIgnoreCase(role))) {
            throw new UserManagementException(BusinessErrorType.FORBIDDEN, BusinessErrorCode.ADMIN_ROLE_REQUIRED, "Administrator role is required");
        }
    }

    static Set<AppRole> mapRoles(Set<String> roles) {
        Objects.requireNonNull(roles, "roles are required");
        if (roles.isEmpty()) {
            throw new UserManagementException(BusinessErrorType.VALIDATION, BusinessErrorCode.USER_ROLE_REQUIRED, "At least one role is required");
        }
        return roles.stream()
                .map(role -> AppRole.valueOf(role.toUpperCase()))
                .collect(Collectors.toSet());
    }

    static UserId userId(UUID value) {
        return new UserId(value);
    }

    static EmailAddress email(String value) {
        return new EmailAddress(value);
    }

    static PasswordHash passwordHash(String value) {
        return new PasswordHash(value);
    }

    static UserStatus status(String value) {
        UserStatus result;

        if (value == null || value.isBlank()) {
            result = null;
        } else {
            result = UserStatus.valueOf(value.toUpperCase());
        }

        return result;
    }

    static com.cybermanager.application.views.users.UserDetailsView toDetails(User user) {
        return new com.cybermanager.application.views.users.UserDetailsView(
                user.id().value(),
                user.email().value(),
                user.firstName(),
                user.lastName(),
                user.roles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.status().name()
        );
    }

    static com.cybermanager.application.views.users.UserSummaryView toSummary(User user) {

        return new com.cybermanager.application.views.users.UserSummaryView(
                user.id().value(),
                user.email().value(),
                user.firstName(),
                user.lastName(),
                user.roles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.status().name()
        );

    }

}


