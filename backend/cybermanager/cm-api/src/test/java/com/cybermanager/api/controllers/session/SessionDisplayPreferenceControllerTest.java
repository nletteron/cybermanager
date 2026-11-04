package com.cybermanager.api.controllers.session;

import com.cybermanager.application.usecases.session.ConfigureSessionDisplayPreferenceUseCase;
import com.cybermanager.application.usecases.session.GetSessionDisplayPreferenceUseCase;
import com.cybermanager.application.views.session.SessionDisplayPreferenceView;
import com.cybermanager.infrastructure.out.adapters.security.users.AuthenticatedActor;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SessionDisplayPreferenceControllerTest {

    private final GetSessionDisplayPreferenceUseCase getUseCase = mock(GetSessionDisplayPreferenceUseCase.class);
    private final ConfigureSessionDisplayPreferenceUseCase configureUseCase = mock(ConfigureSessionDisplayPreferenceUseCase.class);
    private final JwtAccessTokenReader tokenReader = mock(JwtAccessTokenReader.class);
    private MockMvc mvc;

    @BeforeEach
    void configureMvc() {
        when(tokenReader.read("valid")).thenReturn(new AuthenticatedActor("alice@example.test", Set.of("EMPLOYEE")));
        mvc = MockMvcBuilders.standaloneSetup(new SessionDisplayPreferenceController(getUseCase, configureUseCase, tokenReader)).build();
    }

    @Test
    void shouldReturnNoContentWhenTheUserHasNoSavedChoice() throws Exception {
        mvc.perform(get("/api/sessions/display-preference").header("Authorization", "Bearer valid"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(getUseCase).execute(argThat(query -> query.owner().value().equals("alice@example.test")));
    }

    @Test
    void shouldReadTheAuthenticatedUserAndIgnoreAnOwnerInTheQueryString() throws Exception {
        when(getUseCase.execute(any())).thenReturn(new SessionDisplayPreferenceView(true));
        mvc.perform(get("/api/sessions/display-preference?userEmail=bob@example.test").header("Authorization", "Bearer valid"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.groupEntries").value(true));
        verify(getUseCase).execute(argThat(query -> query.owner().value().equals("alice@example.test")));
    }

    @Test
    void shouldWriteTheAuthenticatedUsersChoice() throws Exception {
        when(configureUseCase.execute(any())).thenReturn(new SessionDisplayPreferenceView(false));
        mvc.perform(put("/api/sessions/display-preference").header("Authorization", "Bearer valid")
                        .contentType("application/json").content("{\"groupEntries\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.groupEntries").value(false));
        verify(configureUseCase).execute(argThat(command -> command.owner().value().equals("alice@example.test") && !command.groupEntries()));
    }

    @Test
    void shouldRequireAuthenticationInformation() throws Exception {
        mvc.perform(get("/api/sessions/display-preference")).andExpect(status().isBadRequest());
        verifyNoInteractions(getUseCase, configureUseCase);
    }

}
