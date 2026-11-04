package com.cybermanager.auth.domain.ports;

import com.cybermanager.auth.domain.businessobjects.UserAccount;
import com.cybermanager.auth.domain.valueobjects.EmailAddress;
import com.cybermanager.auth.domain.valueobjects.UserId;

import java.util.Optional;

public interface UserAuthenticationRepository {

    Optional<UserAccount> findByEmail(EmailAddress email);

    Optional<UserAccount> findById(UserId id);

}

