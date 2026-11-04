package com.cybermanager.application.usecases.session;

import com.cybermanager.application.views.session.SessionDisplayPreferenceView;
import com.cybermanager.domain.businessobjects.session.SessionDisplayPreference;
import com.cybermanager.domain.commands.session.ConfigureSessionDisplayPreferenceCommand;
import com.cybermanager.domain.ports.session.SessionDisplayPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfigureSessionDisplayPreferenceUseCase {

    private final SessionDisplayPreferenceRepository repository;

    public ConfigureSessionDisplayPreferenceUseCase(SessionDisplayPreferenceRepository repository) {
        this.repository = repository;
    }

    public SessionDisplayPreferenceView execute(ConfigureSessionDisplayPreferenceCommand command) {
        var saved = repository.save(new SessionDisplayPreference(command.owner(), command.groupEntries()));
        return new SessionDisplayPreferenceView(saved.groupEntries());
    }

}
