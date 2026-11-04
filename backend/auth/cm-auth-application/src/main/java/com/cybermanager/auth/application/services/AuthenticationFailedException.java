package com.cybermanager.auth.application.services;

import java.io.Serial;

public class AuthenticationFailedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationFailedException(String message) {
        super(message);
    }

}

