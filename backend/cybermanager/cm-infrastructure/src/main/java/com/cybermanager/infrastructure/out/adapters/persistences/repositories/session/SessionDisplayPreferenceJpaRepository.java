package com.cybermanager.infrastructure.out.adapters.persistences.repositories.session;

import com.cybermanager.infrastructure.out.entities.session.SessionDisplayPreferenceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionDisplayPreferenceJpaRepository extends JpaRepository<SessionDisplayPreferenceJpaEntity, String> {

}
