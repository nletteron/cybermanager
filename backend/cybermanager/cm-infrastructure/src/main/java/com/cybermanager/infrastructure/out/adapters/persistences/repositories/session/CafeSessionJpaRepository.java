package com.cybermanager.infrastructure.out.adapters.persistences.repositories.session;

import com.cybermanager.infrastructure.out.entities.session.CafeSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CafeSessionJpaRepository extends JpaRepository<CafeSessionJpaEntity, UUID> {

    List<CafeSessionJpaEntity> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    List<CafeSessionJpaEntity> findByEndedAtIsNull();

    List<CafeSessionJpaEntity> findByCustomerIdInOrderByStartedAtDesc(List<UUID> customerIds);

    void deleteByCustomerIdIn(List<UUID> customerIds);

}

