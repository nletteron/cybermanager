package com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer;

import com.cybermanager.infrastructure.out.entities.customer.GeneratedArchiveFileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GeneratedArchiveFileJpaRepository extends JpaRepository<GeneratedArchiveFileJpaEntity, UUID> {

    List<GeneratedArchiveFileJpaEntity> findAllByOrderByGeneratedAtDesc();

}
