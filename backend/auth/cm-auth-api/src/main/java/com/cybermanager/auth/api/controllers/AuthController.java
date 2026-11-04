package com.cybermanager.auth.api.controllers;

import com.cybermanager.auth.api.dtos.AuthenticationResponse;
import com.cybermanager.auth.api.dtos.CurrentUserResponse;
import com.cybermanager.auth.api.dtos.LoginRequest;
import com.cybermanager.auth.api.mappers.AuthApiMapper;
import com.cybermanager.auth.application.usecases.AuthenticateUserUseCase;
import com.cybermanager.auth.application.usecases.LoadCurrentUserUseCase;
import com.cybermanager.auth.domain.queries.LoadCurrentUserQuery;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final LoadCurrentUserUseCase loadCurrentUserUseCase;
    private final AuthApiMapper authApiMapper;

    public AuthController(
            AuthenticateUserUseCase authenticateUserUseCase,
            LoadCurrentUserUseCase loadCurrentUserUseCase,
            AuthApiMapper authApiMapper
    ) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.loadCurrentUserUseCase = loadCurrentUserUseCase;
        this.authApiMapper = authApiMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authApiMapper.toResponse(
                authenticateUserUseCase.execute(authApiMapper.toAuthenticateUserCommand(request))
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(authApiMapper.toResponse(
                loadCurrentUserUseCase.execute(new LoadCurrentUserQuery(extractToken(authorization)))
        ));
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing bearer token");
        }
        return authorization.substring(7);
    }

}



