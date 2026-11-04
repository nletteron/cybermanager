package com.cybermanager.application.usecases.customer;

import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.commands.customer.UpdateCustomerCommentCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.DomainException;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateCustomerCommentUseCaseTest {

    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final UpdateCustomerCommentUseCase useCase = new UpdateCustomerCommentUseCase(customers);

    @Test
    void shouldSaveNormalizedCommentAndPreserveCustomerData() {
        var customer = Customer.createWalkIn("Alice").addSubscriptionMinutes(90);
        when(customers.findById(customer.id())).thenReturn(Optional.of(customer));
        when(customers.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = useCase.execute(new UpdateCustomerCommentCommand(customer.id().value(), "  Note  "));
        assertEquals(customer.id().value(), result.customerId());
        assertEquals("Note", result.comment());
        verify(customers).save(customer.updateComment("Note"));
    }

    @Test
    void shouldClearComment() {
        var customer = Customer.createWalkIn("Alice").updateComment("Ancien");
        when(customers.findById(customer.id())).thenReturn(Optional.of(customer));
        when(customers.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertNull(useCase.execute(new UpdateCustomerCommentCommand(customer.id().value(), " \n ")).comment());
    }

    @Test
    void shouldRejectMissingCustomerWithoutWriting() {
        var error = assertThrows(DomainException.class, () -> useCase.execute(new UpdateCustomerCommentCommand(UUID.randomUUID(), "Note")));
        assertEquals(BusinessErrorCode.CUSTOMER_NOT_FOUND, error.businessErrorCode());
        verify(customers, never()).save(any());
    }

    @Test
    void shouldRejectTooLongCommentWithoutWriting() {
        var customer = Customer.createWalkIn("Alice");
        when(customers.findById(customer.id())).thenReturn(Optional.of(customer));
        assertThrows(DomainException.class, () -> useCase.execute(new UpdateCustomerCommentCommand(customer.id().value(), "a".repeat(2001))));
        verify(customers, never()).save(any());
    }

}
