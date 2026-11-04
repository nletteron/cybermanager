package com.cybermanager.application.usecases.users;

import com.cybermanager.application.views.users.UserDetailsView;
import com.cybermanager.domain.commands.users.ChangeUserStatusCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
public class EnableUserUseCase {

    private final UserRepository userRepository;

    public EnableUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetailsView execute(ChangeUserStatusCommand command) {
        UsersApplicationSupport.requireAdmin(command.actorRoles());
        var user = userRepository.findById(UsersApplicationSupport.userId(command.userId()))
                .orElseThrow(() -> new UserManagementException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.USER_NOT_FOUND, "User not found"));
        return UsersApplicationSupport.toDetails(userRepository.save(user.enable()));
    }

}
