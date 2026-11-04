package com.cybermanager.application.usecases.company;

import com.cybermanager.domain.businessobjects.company.CompanyProfile;
import com.cybermanager.domain.commands.company.CreateCompanyProfileCommand;
import com.cybermanager.domain.commands.company.UpdateCompanyProfileCommand;
import com.cybermanager.domain.ports.company.CompanyProfileRepository;
import com.cybermanager.domain.queries.company.GetCompanyProfileQuery;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompanyProfileUseCaseTest {

    @Test
    void shouldReturnNullWhenCompanyDoesNotExist() {
        CompanyProfileRepository repository = mock(CompanyProfileRepository.class);
        GetCompanyProfileUseCase useCase = new GetCompanyProfileUseCase(repository);

        when(repository.findCurrent()).thenReturn(Optional.empty());

        assertNull(useCase.execute(new GetCompanyProfileQuery()));
    }

    @Test
    void shouldCreateCompanyProfile() {
        CompanyProfileRepository repository = mock(CompanyProfileRepository.class);
        CreateCompanyProfileUseCase useCase = new CreateCompanyProfileUseCase(repository);

        when(repository.findCurrent()).thenReturn(Optional.empty());
        when(repository.save(any(CompanyProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new CreateCompanyProfileCommand(
                "admin@cybermanager.local",
                "CyberManager SARL",
                "12345678901234",
                "0102030405",
                "contact@cybermanager.local",
                "1 rue du Net",
                null,
                "75001",
                "Paris",
                "France"
        ));

        assertEquals("CyberManager SARL", result.legalName());
        assertEquals("12345678901234", result.siret());
    }

    @Test
    void shouldUpdateCompanyProfile() {
        CompanyProfileRepository repository = mock(CompanyProfileRepository.class);
        UpdateCompanyProfileUseCase useCase = new UpdateCompanyProfileUseCase(repository);
        CompanyProfile current = CompanyProfile.create("Old", null, null, null, null, null, null, null, null, LocalDateTime.now().minusDays(1));

        when(repository.findCurrent()).thenReturn(Optional.of(current));
        when(repository.save(any(CompanyProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new UpdateCompanyProfileCommand(
                "admin@cybermanager.local",
                "CyberManager SARL",
                "12345678901234",
                "0102030405",
                "contact@cybermanager.local",
                "1 rue du Net",
                null,
                "75001",
                "Paris",
                "France"
        ));

        assertEquals(current.id(), result.id());
        assertEquals("CyberManager SARL", result.legalName());
    }

}
