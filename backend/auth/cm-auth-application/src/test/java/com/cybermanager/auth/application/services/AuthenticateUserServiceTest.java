package com.cybermanager.auth.application.services;

import com.cybermanager.auth.application.ports.PasswordVerifier;
import com.cybermanager.auth.application.ports.TokenIssuer;
import com.cybermanager.auth.application.usecases.AuthenticateUserUseCase;
import com.cybermanager.auth.domain.businessobjects.UserAccount;
import com.cybermanager.auth.domain.commands.AuthenticateUserCommand;
import com.cybermanager.auth.domain.ports.UserAuthenticationRepository;
import com.cybermanager.auth.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticateUserUseCaseTest {

    @Test
    void shouldAuthenticateActiveUser() {
        UserAuthenticationRepository repository = mock(UserAuthenticationRepository.class);
        PasswordVerifier passwordVerifier = mock(PasswordVerifier.class);
        TokenIssuer tokenIssuer = mock(TokenIssuer.class);
        AuthenticateUserUseCase useCase = new AuthenticateUserUseCase(repository, passwordVerifier, tokenIssuer);

        UserAccount user = new UserAccount(
                new UserId(UUID.randomUUID()),
                new EmailAddress("admin@cybermanager.local"),
                "Admin",
                "Manager",
                new PasswordHash("hash"),
                Set.of(AppRole.ADMIN),
                UserStatus.ACTIVE
        );

        when(repository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordVerifier.matches("admin123", "hash")).thenReturn(true);
        when(tokenIssuer.issue(user)).thenReturn("jwt-token");

        var result = useCase.execute(new AuthenticateUserCommand("admin@cybermanager.local", "admin123"));

        assertEquals("jwt-token", result.token());
        assertEquals("admin@cybermanager.local", result.email());
    }

    @Test
    void shouldRejectDisabledUser() {

        UserAuthenticationRepository repository = mock(UserAuthenticationRepository.class);
        PasswordVerifier passwordVerifier = mock(PasswordVerifier.class);
        TokenIssuer tokenIssuer = mock(TokenIssuer.class);
        AuthenticateUserUseCase useCase = new AuthenticateUserUseCase(repository, passwordVerifier, tokenIssuer);

        UserAccount user = new UserAccount(
                new UserId(UUID.randomUUID()),
                new EmailAddress("disabled@cybermanager.local"),
                "Disabled",
                "User",
                new PasswordHash("hash"),
                Set.of(AppRole.EMPLOYEE),
                UserStatus.DISABLED
        );

        when(repository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThrows(
                AuthenticationFailedException.class,
                () -> useCase.execute(new AuthenticateUserCommand("disabled@cybermanager.local", "pwd"))
        );

    }

}

