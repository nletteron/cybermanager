package com.cybermanager.infrastructure.out.adapters.persistences.session;

import com.cybermanager.domain.businessobjects.session.SessionDisplayPreference;
import com.cybermanager.domain.ports.session.SessionDisplayPreferenceRepository;
import com.cybermanager.domain.valueobjects.users.EmailAddress;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.session.SessionDisplayPreferenceJpaRepository;
import com.cybermanager.infrastructure.out.entities.session.SessionDisplayPreferenceJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SessionDisplayPreferenceRepositoryAdapter implements SessionDisplayPreferenceRepository {

    private final SessionDisplayPreferenceJpaRepository repository;

    public SessionDisplayPreferenceRepositoryAdapter(SessionDisplayPreferenceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SessionDisplayPreference> findByOwner(EmailAddress owner) {
        return repository.findById(owner.value()).map(this::toDomain);
    }

    @Override
    public SessionDisplayPreference save(SessionDisplayPreference preference) {
        var entity = new SessionDisplayPreferenceJpaEntity();
        entity.userEmail = preference.owner().value();
        entity.groupEntries = preference.groupEntries();
        return toDomain(repository.save(entity));
    }

    private SessionDisplayPreference toDomain(SessionDisplayPreferenceJpaEntity entity) {
        return new SessionDisplayPreference(new EmailAddress(entity.userEmail), entity.groupEntries);
    }

}
