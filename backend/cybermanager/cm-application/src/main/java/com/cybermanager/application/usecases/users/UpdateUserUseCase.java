package com.cybermanager.application.usecases.users;

import com.cybermanager.application.ports.users.PasswordHasher;
import com.cybermanager.application.views.users.UserDetailsView;
import com.cybermanager.domain.businessobjects.users.User;
import com.cybermanager.domain.commands.users.UpdateUserCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UpdateUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public UserDetailsView execute(UpdateUserCommand command) {
        UsersApplicationSupport.requireAdmin(command.actorRoles());
        var userId = UsersApplicationSupport.userId(command.userId());
        User current = userRepository.findById(userId)
                .orElseThrow(() -> new UserManagementException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.USER_NOT_FOUND, "User not found"));
        var email = UsersApplicationSupport.email(command.email());
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new UserManagementException(BusinessErrorType.CONFLICT, BusinessErrorCode.USER_EMAIL_EXISTS, "Email already exists");
        }
        var passwordHash = command.password() == null || command.password().isBlank()
                ? null
                : UsersApplicationSupport.passwordHash(passwordHasher.hash(command.password()));

        User updated = current.update(
                email,
                command.firstName(),
                command.lastName(),
                UsersApplicationSupport.mapRoles(command.roles()),
                passwordHash
        );
        return UsersApplicationSupport.toDetails(userRepository.save(updated));
    }

}
