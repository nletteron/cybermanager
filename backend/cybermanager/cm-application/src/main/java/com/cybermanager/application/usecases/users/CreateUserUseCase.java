package com.cybermanager.application.usecases.users;

import com.cybermanager.application.ports.users.PasswordHasher;
import com.cybermanager.application.views.users.UserDetailsView;
import com.cybermanager.domain.businessobjects.users.User;
import com.cybermanager.domain.commands.users.CreateUserCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.users.UserRepository;
import com.cybermanager.domain.valueobjects.users.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public CreateUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public UserDetailsView execute(CreateUserCommand command) {
        UsersApplicationSupport.requireAdmin(command.actorRoles());
        var email = UsersApplicationSupport.email(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new UserManagementException(BusinessErrorType.CONFLICT, BusinessErrorCode.USER_EMAIL_EXISTS, "Email already exists");
        }

        User user = User.create(
                UserId.newId(),
                email,
                command.firstName(),
                command.lastName(),
                UsersApplicationSupport.passwordHash(passwordHasher.hash(command.password())),
                UsersApplicationSupport.mapRoles(command.roles())
        );

        return UsersApplicationSupport.toDetails(userRepository.save(user));
    }

}
