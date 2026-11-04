package com.cybermanager.domain.businessobjects.customer;

import com.cybermanager.domain.ContractTestSupport;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void shouldExposeDeclaredContract() {
        ContractTestSupport.assertRecordContract(Customer.class);
    }

    @Test
    void shouldNormalizeAndClearComment() {
        var customer = Customer.createWalkIn("Alice").updateComment("  Ligne 1\nLigne 2  ");
        assertEquals("Ligne 1\nLigne 2", customer.comment());
        assertNull(customer.updateComment(" \n ").comment());
        assertNull(customer.updateComment(null).comment());
        assertEquals(2000, customer.updateComment("a".repeat(2000)).comment().length());
    }

    @Test
    void shouldRejectCommentExceedingLimit() {
        var customer = Customer.createWalkIn("Alice");
        var exception = assertThrows(DomainException.class, () -> customer.updateComment("a".repeat(2001)));
        assertEquals(BusinessErrorCode.CUSTOMER_COMMENT_TOO_LONG, exception.businessErrorCode());
    }

    @Test
    void shouldPreserveCommentThroughCustomerChanges() {
        var customer = Customer.createWalkIn("Alice").updateComment("Note client");
        assertEquals("Note client", customer.updateName("Alice B").comment());
        assertEquals("Note client", customer.convertToSubscriber(60).comment());
        assertEquals("Note client", customer.deductMinutes(2).comment());
        assertEquals("Note client", customer.addSubscriptionMinutes(60).comment());
        assertEquals("Note client", customer.removeSubscriptionMinutes(10).comment());
        assertEquals("Note client", customer.withRemainingMinutes(15).comment());
    }

}
