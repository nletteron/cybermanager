package com.cybermanager.infrastructure.out.adapters.persistences.repositories.users;

import com.cybermanager.infrastructure.out.entities.users.UserJpaEntity;
import com.cybermanager.infrastructure.out.entities.users.UserStatusJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    Optional<UserJpaEntity> findByEmail(String email);

    List<UserJpaEntity> findByStatus(UserStatusJpa status);

}

