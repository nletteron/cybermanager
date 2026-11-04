package com.cybermanager.infrastructure.out.adapters.persistences.repositories.company;

import com.cybermanager.infrastructure.out.entities.company.CompanyProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyProfileJpaRepository extends JpaRepository<CompanyProfileJpaEntity, UUID> {

    Optional<CompanyProfileJpaEntity> findTopByOrderByUpdatedAtDesc();

}
