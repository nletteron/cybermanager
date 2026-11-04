package com.cybermanager.application.usecases.session;

import com.cybermanager.application.views.session.SessionDisplayPreferenceView;
import com.cybermanager.domain.ports.session.SessionDisplayPreferenceRepository;
import com.cybermanager.domain.queries.session.GetSessionDisplayPreferenceQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetSessionDisplayPreferenceUseCase {

    private final SessionDisplayPreferenceRepository repository;

    public GetSessionDisplayPreferenceUseCase(SessionDisplayPreferenceRepository repository) {
        this.repository = repository;
    }

    public SessionDisplayPreferenceView execute(GetSessionDisplayPreferenceQuery query) {
        return repository.findByOwner(query.owner())
                .map(preference -> new SessionDisplayPreferenceView(preference.groupEntries()))
                .orElse(null);
    }

}
