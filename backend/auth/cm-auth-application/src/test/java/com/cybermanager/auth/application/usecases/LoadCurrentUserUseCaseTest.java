package com.cybermanager.auth.application.usecases;

import com.cybermanager.auth.application.ports.TokenReader;
import com.cybermanager.auth.application.services.AuthenticationFailedException;
import com.cybermanager.auth.domain.businessobjects.UserAccount;
import com.cybermanager.auth.domain.ports.UserAuthenticationRepository;
import com.cybermanager.auth.domain.queries.LoadCurrentUserQuery;
import com.cybermanager.auth.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoadCurrentUserUseCaseTest {

    @Test
    void shouldLoadCurrentActiveUserFromToken() {
        TokenReader tokenReader = mock(TokenReader.class);
        UserAuthenticationRepository repository = mock(UserAuthenticationRepository.class);
        LoadCurrentUserUseCase useCase = new LoadCurrentUserUseCase(tokenReader, repository);
        UUID userId = UUID.randomUUID();

        when(tokenReader.readUserId("token")).thenReturn(userId);
        when(repository.findById(new UserId(userId))).thenReturn(Optional.of(userAccount(userId, UserStatus.ACTIVE)));

        var result = useCase.execute(new LoadCurrentUserQuery("token"));

        assertEquals(userId, result.userId());
        assertEquals("admin@cybermanager.local", result.email());
        assertEquals(Set.of("ADMIN"), result.roles());
    }

    @Test
    void shouldRejectDisabledCurrentUser() {
        TokenReader tokenReader = mock(TokenReader.class);
        UserAuthenticationRepository repository = mock(UserAuthenticationRepository.class);
        LoadCurrentUserUseCase useCase = new LoadCurrentUserUseCase(tokenReader, repository);
        UUID userId = UUID.randomUUID();

        when(tokenReader.readUserId("token")).thenReturn(userId);
        when(repository.findById(new UserId(userId))).thenReturn(Optional.of(userAccount(userId, UserStatus.DISABLED)));

        assertThrows(AuthenticationFailedException.class, () -> useCase.execute(new LoadCurrentUserQuery("token")));
    }

    private UserAccount userAccount(UUID id, UserStatus status) {
        return new UserAccount(
                new UserId(id),
                new EmailAddress("admin@cybermanager.local"),
                "Alice",
                "Martin",
                new PasswordHash("hashed"),
                Set.of(AppRole.ADMIN),
                status
        );
    }

}
