package com.cybermanager.application.usecases.session;

import com.cybermanager.domain.businessobjects.session.SessionDisplayPreference;
import com.cybermanager.domain.commands.session.ConfigureSessionDisplayPreferenceCommand;
import com.cybermanager.domain.ports.session.SessionDisplayPreferenceRepository;
import com.cybermanager.domain.valueobjects.users.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ConfigureSessionDisplayPreferenceUseCaseTest {

    @Test
    void shouldPersistBothExplicitChoicesForTheOwner() {
        var repository = mock(SessionDisplayPreferenceRepository.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var useCase = new ConfigureSessionDisplayPreferenceUseCase(repository);
        var owner = new EmailAddress("alice@example.test");

        assertTrue(useCase.execute(new ConfigureSessionDisplayPreferenceCommand(owner, true)).groupEntries());
        verify(repository).save(new SessionDisplayPreference(owner, true));
        assertFalse(useCase.execute(new ConfigureSessionDisplayPreferenceCommand(owner, false)).groupEntries());
        verify(repository).save(new SessionDisplayPreference(owner, false));
    }

}
