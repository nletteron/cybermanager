package com.cybermanager.api.mappers.users;

import com.cybermanager.api.dtos.users.CreateUserRequest;
import com.cybermanager.api.dtos.users.UpdateUserRequest;
import com.cybermanager.api.dtos.users.UserResponse;
import com.cybermanager.application.views.users.UserDetailsView;
import com.cybermanager.application.views.users.UserSummaryView;
import com.cybermanager.domain.commands.users.ChangeUserStatusCommand;
import com.cybermanager.domain.commands.users.CreateUserCommand;
import com.cybermanager.domain.commands.users.DeleteUserCommand;
import com.cybermanager.domain.commands.users.UpdateUserCommand;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserApiMapper {

    UserResponse toResponse(UserDetailsView view);

    UserResponse toResponse(UserSummaryView view);

    default CreateUserCommand toCreateUserCommand(String actorEmail, Set<String> actorRoles, CreateUserRequest request) {
        return new CreateUserCommand(
                actorEmail,
                actorRoles,
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password(),
                request.roles()
        );
    }

    default UpdateUserCommand toUpdateUserCommand(String actorEmail, Set<String> actorRoles, UUID userId, UpdateUserRequest request) {
        return new UpdateUserCommand(
                actorEmail,
                actorRoles,
                userId,
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password(),
                request.roles()
        );
    }

    default ChangeUserStatusCommand toChangeUserStatusCommand(String actorEmail, Set<String> actorRoles, UUID userId) {
        return new ChangeUserStatusCommand(actorEmail, actorRoles, userId);
    }

    default DeleteUserCommand toDeleteUserCommand(String actorEmail, Set<String> actorRoles, UUID userId) {
        return new DeleteUserCommand(actorEmail, actorRoles, userId);
    }

}

