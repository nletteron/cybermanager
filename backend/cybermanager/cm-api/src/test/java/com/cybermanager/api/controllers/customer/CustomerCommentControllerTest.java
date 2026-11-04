package com.cybermanager.api.controllers.customer;

import com.cybermanager.application.usecases.customer.GetCustomerCommentUseCase;
import com.cybermanager.application.usecases.customer.UpdateCustomerCommentUseCase;
import com.cybermanager.application.views.customer.CustomerCommentView;
import com.cybermanager.domain.commands.customer.UpdateCustomerCommentCommand;
import com.cybermanager.domain.queries.customer.GetCustomerCommentQuery;
import com.cybermanager.infrastructure.out.adapters.security.users.AuthenticatedActor;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerCommentControllerTest {

    @Test
    void shouldReadAndUpdateCommentThroughAuthenticatedDtoEndpoints() throws Exception {
        var get = mock(GetCustomerCommentUseCase.class);
        var update = mock(UpdateCustomerCommentUseCase.class);
        var tokens = mock(JwtAccessTokenReader.class);
        when(tokens.read("token")).thenReturn(new AuthenticatedActor("employee@local", Set.of("EMPLOYEE")));
        var id = UUID.randomUUID();
        when(get.execute(new GetCustomerCommentQuery(id))).thenReturn(new CustomerCommentView(id, "Note"));
        when(update.execute(new UpdateCustomerCommentCommand(id, "Nouvelle"))).thenReturn(new CustomerCommentView(id, "Nouvelle"));
        var mvc = MockMvcBuilders.standaloneSetup(new CustomerCommentController(get, update, tokens)).build();
        mvc.perform(get("/api/customers/{id}/comment", id).header("Authorization", "Bearer token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.customerId").value(id.toString())).andExpect(jsonPath("$.comment").value("Note"));
        mvc.perform(patch("/api/customers/{id}/comment", id).header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"comment\":\"Nouvelle\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.comment").value("Nouvelle"));
        mvc.perform(patch("/api/customers/{id}/comment", id).header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"comment\":\"" + "a".repeat(2001) + "\"}"))
                .andExpect(status().isBadRequest());
        verify(tokens, times(2)).read("token");
    }

}
