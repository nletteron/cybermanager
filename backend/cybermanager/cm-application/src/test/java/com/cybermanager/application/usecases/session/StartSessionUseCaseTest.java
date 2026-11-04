package com.cybermanager.application.usecases.session;

import com.cybermanager.application.ApplicationClassContractAssertions;
import com.cybermanager.application.services.sales.InvoiceNumberGenerator;
import com.cybermanager.application.services.sales.InvoicePdfRenderer;
import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.businessobjects.sales.ConnectionPricingRule;
import com.cybermanager.domain.businessobjects.session.CafeSession;
import com.cybermanager.domain.commands.session.StartSessionCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.DomainException;
import com.cybermanager.domain.ports.customer.CustomerRepository;
import com.cybermanager.domain.ports.customer.DebtRepository;
import com.cybermanager.domain.ports.sales.ConnectionPricingRepository;
import com.cybermanager.domain.ports.sales.InvoiceRepository;
import com.cybermanager.domain.ports.sales.SaleRepository;
import com.cybermanager.domain.ports.session.CafeSessionRepository;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.valueobjects.customer.CustomerStatus;
import com.cybermanager.domain.valueobjects.sales.ConnectionPricingTier;
import com.cybermanager.domain.valueobjects.shared.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StartSessionUseCaseTest {

    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final CafeSessionRepository sessions = mock(CafeSessionRepository.class);
    private final ConnectionPricingRepository pricing = mock(ConnectionPricingRepository.class);

    @Test
    void shouldExposeSingleExecuteMethod() {
        ApplicationClassContractAssertions.assertSinglePublicExecuteMethod(StartSessionUseCase.class);
    }

    @Test
    void shouldReuseExistingWalkInCustomerInsteadOfCreatingAnotherCustomer() {
        var customer = Customer.createWalkIn("Alice").updateComment("Note conservee");
        when(customers.findById(customer.id())).thenReturn(Optional.of(customer));
        when(sessions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pricing.getCurrentRule()).thenReturn(new ConnectionPricingRule(List.of(new ConnectionPricingTier(30, Money.of("1")))));
        var view = useCase().execute(new StartSessionCommand(customer.id().value(), null));
        assertEquals(customer.id().value(), view.customerId());
        assertEquals("WALK_IN", view.customerType());
        verify(customers, never()).save(any());
    }

    @Test
    void shouldKeepSubscriberSelectionAvailable() {
        var customer = Customer.createWalkIn("Alice").addSubscriptionMinutes(90);
        when(customers.findById(customer.id())).thenReturn(Optional.of(customer));
        when(sessions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals("SUBSCRIBER", useCase().execute(new StartSessionCommand(customer.id().value(), null)).customerType());
        verify(customers, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateSessionForExistingWalkInCustomer() {
        var customer = Customer.createWalkIn("Alice");
        when(customers.findById(customer.id())).thenReturn(Optional.of(customer));
        when(sessions.findAll()).thenReturn(List.of(CafeSession.start(customer.id(), "SESSION", LocalDateTime.now())));
        var exception = assertThrows(DomainException.class, () -> useCase().execute(new StartSessionCommand(customer.id().value(), null)));
        assertEquals(BusinessErrorCode.SESSION_ALREADY_EXISTS_FOR_CUSTOMER, exception.businessErrorCode());
        verify(sessions, never()).save(any());
    }

    @Test
    void shouldRejectInactiveCustomer() {
        var original = Customer.createWalkIn("Alice");
        var customer = new Customer(original.id(), original.name(), original.type(), CustomerStatus.INACTIVE, 0);
        when(customers.findById(customer.id())).thenReturn(Optional.of(customer));
        var exception = assertThrows(DomainException.class, () -> useCase().execute(new StartSessionCommand(customer.id().value(), null)));
        assertEquals(BusinessErrorCode.CUSTOMER_INACTIVE, exception.businessErrorCode());
        verify(sessions, never()).save(any());
    }

    private StartSessionUseCase useCase() {
        return new StartSessionUseCase(sessions, customers, pricing, mock(DebtRepository.class), mock(SaleRepository.class),
                mock(SubscriptionOfferRepository.class), mock(InvoiceRepository.class), mock(InvoicePdfRenderer.class), mock(InvoiceNumberGenerator.class));
    }

}
