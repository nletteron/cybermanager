package com.cybermanager.application.usecases.users;

import com.cybermanager.application.views.users.UserSummaryView;
import com.cybermanager.domain.ports.users.UserRepository;
import com.cybermanager.domain.queries.users.SearchUsersQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional(readOnly = true)
public class SearchUsersUseCase {

    private final UserRepository userRepository;

    public SearchUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserSummaryView> execute(SearchUsersQuery query) {
        UsersApplicationSupport.requireAdmin(query.actorRoles());
        return userRepository.search(query.term(), UsersApplicationSupport.status(query.status()))
                .stream()
                .map(UsersApplicationSupport::toSummary)
                .toList();
    }

}
