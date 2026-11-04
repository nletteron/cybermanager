package com.cybermanager.domain.ports.session;

import com.cybermanager.domain.businessobjects.session.SessionDisplayPreference;
import com.cybermanager.domain.valueobjects.users.EmailAddress;

import java.util.Optional;

public interface SessionDisplayPreferenceRepository {

    Optional<SessionDisplayPreference> findByOwner(EmailAddress owner);

    SessionDisplayPreference save(SessionDisplayPreference preference);

}
