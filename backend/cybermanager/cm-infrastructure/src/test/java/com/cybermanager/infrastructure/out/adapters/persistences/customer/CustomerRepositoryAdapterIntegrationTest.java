package com.cybermanager.infrastructure.out.adapters.persistences.customer;

import com.cybermanager.domain.businessobjects.customer.Customer;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.CustomerStatus;
import com.cybermanager.domain.valueobjects.customer.CustomerType;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CustomerRepositoryAdapter adapter;
    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveSearchAndDeleteCustomers() {
        Customer subscriber = adapter.save(new Customer(CustomerId.newId(), "Alice Abonnee", CustomerType.SUBSCRIBER, CustomerStatus.ACTIVE, 90));
        Customer walkIn = adapter.save(Customer.createWalkIn("Bob Passage"));

        var result = adapter.search("ali", CustomerType.SUBSCRIBER);
        adapter.deleteByIds(List.of(walkIn.id()));
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, result.size());
        assertEquals(subscriber.id(), result.get(0).id());
        assertTrue(adapter.findById(walkIn.id()).isEmpty());
    }

    @Test
    void shouldReturnEmptyForMissingCustomer() {
        assertTrue(adapter.findById(CustomerId.newId()).isEmpty());
        assertTrue(adapter.search("unknown", null).isEmpty());
    }

    @Test
    void shouldPersistAndClearCommentWithoutLosingCustomerCredit() {
        var customer = adapter.save(Customer.createWalkIn("Alice").addSubscriptionMinutes(60).updateComment(" Note client "));
        entityManager.flush();
        entityManager.clear();
        var loaded = adapter.findById(customer.id()).orElseThrow();
        assertEquals("Note client", loaded.comment());
        adapter.save(loaded.deductMinutes(5));
        entityManager.flush();
        entityManager.clear();
        var updated = adapter.findById(customer.id()).orElseThrow();
        assertEquals(55, updated.remainingMinutes());
        assertEquals("Note client", updated.comment());
        adapter.save(updated.updateComment(null));
        entityManager.flush();
        entityManager.clear();
        assertTrue(adapter.findById(customer.id()).orElseThrow().comment() == null);
    }

}
