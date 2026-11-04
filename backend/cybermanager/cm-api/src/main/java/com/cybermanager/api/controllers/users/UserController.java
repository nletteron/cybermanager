package com.cybermanager.api.controllers.users;

import com.cybermanager.api.abstracts.ApiSupport;
import com.cybermanager.api.dtos.users.CreateUserRequest;
import com.cybermanager.api.dtos.users.UpdateUserRequest;
import com.cybermanager.api.dtos.users.UserResponse;
import com.cybermanager.api.mappers.users.UserApiMapper;
import com.cybermanager.application.usecases.users.*;
import com.cybermanager.domain.queries.users.GetUserDetailsQuery;
import com.cybermanager.domain.queries.users.SearchUsersQuery;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final SearchUsersUseCase searchUsersUseCase;
    private final GetUserDetailsUseCase getUserDetailsUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final EnableUserUseCase enableUserUseCase;
    private final DisableUserUseCase disableUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final JwtAccessTokenReader jwtAccessTokenReader;
    private final UserApiMapper userApiMapper;

    public UserController(
            SearchUsersUseCase searchUsersUseCase,
            GetUserDetailsUseCase getUserDetailsUseCase,
            CreateUserUseCase createUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            EnableUserUseCase enableUserUseCase,
            DisableUserUseCase disableUserUseCase,
            DeleteUserUseCase deleteUserUseCase,
            JwtAccessTokenReader jwtAccessTokenReader,
            UserApiMapper userApiMapper
    ) {
        this.searchUsersUseCase = searchUsersUseCase;
        this.getUserDetailsUseCase = getUserDetailsUseCase;
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.enableUserUseCase = enableUserUseCase;
        this.disableUserUseCase = disableUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.jwtAccessTokenReader = jwtAccessTokenReader;
        this.userApiMapper = userApiMapper;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> search(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(name = "term", required = false) String term,
            @RequestParam(name = "status", required = false) String status
    ) {
        var actor = ApiSupport.actor(authorization, jwtAccessTokenReader);
        var result = searchUsersUseCase.execute(new SearchUsersQuery(actor.email(), actor.roles(), term, status))
                .stream()
                .map(userApiMapper::toResponse)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") UUID id
    ) {
        var actor = ApiSupport.actor(authorization, jwtAccessTokenReader);
        return ResponseEntity.ok(userApiMapper.toResponse(
                getUserDetailsUseCase.execute(new GetUserDetailsQuery(actor.email(), actor.roles(), id))
        ));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CreateUserRequest request
    ) {
        var actor = ApiSupport.actor(authorization, jwtAccessTokenReader);
        return ResponseEntity.ok(userApiMapper.toResponse(
                createUserUseCase.execute(userApiMapper.toCreateUserCommand(actor.email(), actor.roles(), request))
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        var actor = ApiSupport.actor(authorization, jwtAccessTokenReader);
        return ResponseEntity.ok(userApiMapper.toResponse(
                updateUserUseCase.execute(userApiMapper.toUpdateUserCommand(actor.email(), actor.roles(), id, request))
        ));
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<UserResponse> enable(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id) {
        var actor = ApiSupport.actor(authorization, jwtAccessTokenReader);
        return ResponseEntity.ok(userApiMapper.toResponse(
                enableUserUseCase.execute(userApiMapper.toChangeUserStatusCommand(actor.email(), actor.roles(), id))
        ));
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<UserResponse> disable(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id) {
        var actor = ApiSupport.actor(authorization, jwtAccessTokenReader);
        return ResponseEntity.ok(userApiMapper.toResponse(
                disableUserUseCase.execute(userApiMapper.toChangeUserStatusCommand(actor.email(), actor.roles(), id))
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id) {
        var actor = ApiSupport.actor(authorization, jwtAccessTokenReader);
        deleteUserUseCase.execute(userApiMapper.toDeleteUserCommand(actor.email(), actor.roles(), id));
        return ResponseEntity.noContent().build();
    }

}



