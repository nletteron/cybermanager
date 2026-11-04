package com.cybermanager.application.usecases.users;

import com.cybermanager.application.ports.users.PasswordHasher;
import com.cybermanager.domain.businessobjects.users.User;
import com.cybermanager.domain.commands.users.CreateUserCommand;
import com.cybermanager.domain.ports.users.UserRepository;
import com.cybermanager.domain.valueobjects.users.EmailAddress;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateUserUseCaseTest {

    @Test
    void shouldCreateUserForAdmin() {
        UserRepository repository = mock(UserRepository.class);
        PasswordHasher passwordHasher = mock(PasswordHasher.class);
        CreateUserUseCase useCase = new CreateUserUseCase(repository, passwordHasher);

        when(repository.existsByEmail(any(EmailAddress.class))).thenReturn(false);
        when(passwordHasher.hash("password")).thenReturn("hashed");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new CreateUserCommand(
                "admin@cybermanager.local",
                Set.of("ADMIN"),
                "employee@cybermanager.local",
                "Alice",
                "Martin",
                "password",
                Set.of("EMPLOYEE")
        ));

        assertEquals("employee@cybermanager.local", result.email());
        assertEquals("ACTIVE", result.status());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        UserRepository repository = mock(UserRepository.class);
        PasswordHasher passwordHasher = mock(PasswordHasher.class);
        CreateUserUseCase useCase = new CreateUserUseCase(repository, passwordHasher);

        when(repository.existsByEmail(any(EmailAddress.class))).thenReturn(true);

        assertThrows(UserManagementException.class, () -> useCase.execute(new CreateUserCommand(
                "admin@cybermanager.local",
                Set.of("ADMIN"),
                "employee@cybermanager.local",
                "Alice",
                "Martin",
                "password",
                Set.of("EMPLOYEE")
        )));
    }

}
