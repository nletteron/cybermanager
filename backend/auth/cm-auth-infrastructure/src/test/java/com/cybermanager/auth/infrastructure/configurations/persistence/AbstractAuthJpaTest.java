package com.cybermanager.auth.infrastructure.configurations.persistence;

import com.cybermanager.auth.infrastructure.out.adapters.persistences.UserAuthenticationRepositoryAdapter;
import com.cybermanager.auth.infrastructure.out.entities.AppRoleJpa;
import com.cybermanager.auth.infrastructure.out.entities.UserJpaEntity;
import com.cybermanager.auth.infrastructure.out.entities.UserStatusJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.util.Set;
import java.util.UUID;

@DataJpaTest
@ContextConfiguration(classes = JpaTestApplication.class)
@Import(UserAuthenticationRepositoryAdapter.class)
public abstract class AbstractAuthJpaTest {

    protected UserJpaEntity user(UUID id, String email, UserStatusJpa status, Set<AppRoleJpa> roles) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(id);
        entity.setEmail(email);
        entity.setFirstName(email != null && email.startsWith("employee") ? "Employee" : "Admin");
        entity.setLastName("User");
        entity.setPasswordHash("{bcrypt}hash");
        entity.setStatus(status);
        entity.setRoles(roles);
        return entity;
    }

}
