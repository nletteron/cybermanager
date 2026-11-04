package com.cybermanager.application.usecases.users;

import com.cybermanager.application.views.users.UserDetailsView;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.users.UserRepository;
import com.cybermanager.domain.queries.users.GetUserDetailsQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class GetUserDetailsUseCase {

    private final UserRepository userRepository;

    public GetUserDetailsUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetailsView execute(GetUserDetailsQuery query) {
        UsersApplicationSupport.requireAdmin(query.actorRoles());
        var user = userRepository.findById(UsersApplicationSupport.userId(query.userId()))
                .orElseThrow(() -> new UserManagementException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.USER_NOT_FOUND, "User not found"));
        return UsersApplicationSupport.toDetails(user);
    }

}
