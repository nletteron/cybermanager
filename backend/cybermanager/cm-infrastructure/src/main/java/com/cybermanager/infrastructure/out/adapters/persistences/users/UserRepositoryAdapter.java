package com.cybermanager.infrastructure.out.adapters.persistences.users;

import com.cybermanager.domain.businessobjects.users.User;
import com.cybermanager.domain.ports.users.UserRepository;
import com.cybermanager.domain.valueobjects.users.*;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.users.UserJpaRepository;
import com.cybermanager.infrastructure.out.entities.users.AppRoleJpa;
import com.cybermanager.infrastructure.out.entities.users.UserJpaEntity;
import com.cybermanager.infrastructure.out.entities.users.UserStatusJpa;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        return toDomain(userJpaRepository.save(toEntity(user)));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userJpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public void deleteById(UserId id) {
        userJpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return userJpaRepository.existsByEmail(email.value());
    }

    @Override
    public boolean existsByEmailAndIdNot(EmailAddress email, UserId userId) {
        return userJpaRepository.existsByEmailAndIdNot(email.value(), userId.value());
    }

    @Override
    public List<User> search(String term, UserStatus status) {
        String normalizedTerm = term == null ? "" : term.trim().toLowerCase();
        List<UserJpaEntity> source = status == null
                ? userJpaRepository.findAll()
                : userJpaRepository.findByStatus(UserStatusJpa.valueOf(status.name()));
        return source.stream()
                .filter(entity -> normalizedTerm.isBlank()
                        || entity.getEmail().toLowerCase().contains(normalizedTerm)
                        || entity.getFirstName().toLowerCase().contains(normalizedTerm)
                        || entity.getLastName().toLowerCase().contains(normalizedTerm))
                .map(this::toDomain)
                .toList();
    }

    private UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.id().value());
        entity.setEmail(user.email().value());
        entity.setFirstName(user.firstName());
        entity.setLastName(user.lastName());
        entity.setPasswordHash(user.passwordHash().value());
        entity.setStatus(UserStatusJpa.valueOf(user.status().name()));
        entity.setRoles(user.roles().stream().map(role -> AppRoleJpa.valueOf(role.name())).collect(Collectors.toSet()));
        return entity;
    }

    private User toDomain(UserJpaEntity entity) {
        return User.rehydrate(
                new UserId(entity.getId()),
                new EmailAddress(entity.getEmail()),
                entity.getFirstName(),
                entity.getLastName(),
                new PasswordHash(entity.getPasswordHash()),
                entity.getRoles().stream().map(role -> AppRole.valueOf(role.name())).collect(Collectors.toSet()),
                UserStatus.valueOf(entity.getStatus().name())
        );
    }

}



