package com.cybermanager.application.usecases.users;

import com.cybermanager.domain.commands.users.DeleteUserCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
public class DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(DeleteUserCommand command) {
        UsersApplicationSupport.requireAdmin(command.actorRoles());
        var userId = UsersApplicationSupport.userId(command.userId());
        userRepository.findById(userId)
                .orElseThrow(() -> new UserManagementException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.USER_NOT_FOUND, "User not found"));
        userRepository.deleteById(userId);
    }

}
