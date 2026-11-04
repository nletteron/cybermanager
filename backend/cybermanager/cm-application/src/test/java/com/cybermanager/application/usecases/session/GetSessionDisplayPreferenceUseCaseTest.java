package com.cybermanager.application.usecases.session;

import com.cybermanager.domain.businessobjects.session.SessionDisplayPreference;
import com.cybermanager.domain.ports.session.SessionDisplayPreferenceRepository;
import com.cybermanager.domain.queries.session.GetSessionDisplayPreferenceQuery;
import com.cybermanager.domain.valueobjects.users.EmailAddress;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetSessionDisplayPreferenceUseCaseTest {

    @Test
    void shouldReturnAbsenceWithoutCreatingADefaultAndReadTheStoredChoice() {
        var repository = mock(SessionDisplayPreferenceRepository.class);
        var owner = new EmailAddress("alice@example.test");
        var useCase = new GetSessionDisplayPreferenceUseCase(repository);
        var query = new GetSessionDisplayPreferenceQuery(owner);
        when(repository.findByOwner(owner)).thenReturn(Optional.empty());
        assertNull(useCase.execute(query));
        verify(repository, never()).save(any());

        when(repository.findByOwner(owner)).thenReturn(Optional.of(new SessionDisplayPreference(owner, true)));
        assertTrue(useCase.execute(query).groupEntries());
        when(repository.findByOwner(owner)).thenReturn(Optional.of(new SessionDisplayPreference(owner, false)));
        assertFalse(useCase.execute(query).groupEntries());
    }

}
