package com.cybermanager.domain.ports.users;

import com.cybermanager.domain.businessobjects.users.User;
import com.cybermanager.domain.valueobjects.users.EmailAddress;
import com.cybermanager.domain.valueobjects.users.UserId;
import com.cybermanager.domain.valueobjects.users.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    void deleteById(UserId id);

    boolean existsByEmail(EmailAddress email);

    boolean existsByEmailAndIdNot(EmailAddress email, UserId userId);

    List<User> search(String term, UserStatus status);

}

