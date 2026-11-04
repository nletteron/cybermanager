package com.cybermanager.infrastructure.out.adapters.persistences.session;

import com.cybermanager.domain.businessobjects.session.SessionDisplayPreference;
import com.cybermanager.domain.valueobjects.users.EmailAddress;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.session.SessionDisplayPreferenceJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@Import(SessionDisplayPreferenceRepositoryAdapter.class)
class SessionDisplayPreferenceRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private SessionDisplayPreferenceRepositoryAdapter adapter;
    @Autowired
    private SessionDisplayPreferenceJpaRepository repository;
    @Autowired
    private TestEntityManager manager;

    @Test
    void shouldRestoreChoicesUsingANewAdapterAndKeepUsersIsolated() {
        var alice = new EmailAddress("ALICE@example.test");
        var bob = new EmailAddress("bob@example.test");
        assertTrue(adapter.findByOwner(alice).isEmpty());
        assertEquals(0, repository.count());
        adapter.save(new SessionDisplayPreference(alice, true));
        adapter.save(new SessionDisplayPreference(bob, false));
        manager.flush();
        manager.clear();

        var reconnected = new SessionDisplayPreferenceRepositoryAdapter(repository);
        assertTrue(reconnected.findByOwner(new EmailAddress("alice@example.test")).orElseThrow().groupEntries());
        assertFalse(reconnected.findByOwner(bob).orElseThrow().groupEntries());
        reconnected.save(new SessionDisplayPreference(alice, false));
        manager.flush();
        manager.clear();
        assertFalse(adapter.findByOwner(alice).orElseThrow().groupEntries());
        assertFalse(adapter.findByOwner(bob).orElseThrow().groupEntries());
        assertEquals(2, repository.count());
    }

}
