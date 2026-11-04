package com.cybermanager.auth.application.ports;

import com.cybermanager.auth.domain.businessobjects.UserAccount;

public interface TokenIssuer {

    String issue(UserAccount userAccount);

}

