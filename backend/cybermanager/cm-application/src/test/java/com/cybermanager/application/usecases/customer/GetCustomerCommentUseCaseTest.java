package com.cybermanager.application.usecases.customer;

import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.exceptions.DomainException;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.queries.customer.GetCustomerCommentQuery;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GetCustomerCommentUseCaseTest {

    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final GetCustomerCommentUseCase useCase = new GetCustomerCommentUseCase(customers);

    @Test
    void shouldReadCustomerCommentAndRepresentAbsenceAsNull() {
        var customer = Customer.createWalkIn("Alice");
        when(customers.findById(customer.id()))
                .thenReturn(Optional.of(customer))
                .thenReturn(Optional.of(customer.updateComment("Note")));
        var query = new GetCustomerCommentQuery(customer.id().value());
        assertNull(useCase.execute(query).comment());
        assertEquals("Note", useCase.execute(query).comment());
        verify(customers, never()).save(any());
    }

    @Test
    void shouldRejectUnknownCustomer() {
        assertThrows(DomainException.class, () -> useCase.execute(new GetCustomerCommentQuery(UUID.randomUUID())));
    }

}
