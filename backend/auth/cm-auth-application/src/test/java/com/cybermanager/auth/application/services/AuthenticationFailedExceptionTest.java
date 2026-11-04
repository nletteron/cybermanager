package com.cybermanager.auth.application.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthenticationFailedExceptionTest {

    @Test
    void shouldExposeFailureMessage() {
        AuthenticationFailedException result = new AuthenticationFailedException("Invalid credentials");

        assertEquals("Invalid credentials", result.getMessage());
    }

}
