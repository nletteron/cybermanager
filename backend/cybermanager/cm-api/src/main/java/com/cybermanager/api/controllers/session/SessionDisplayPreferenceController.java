package com.cybermanager.api.controllers.session;

import com.cybermanager.api.abstracts.ApiSupport;
import com.cybermanager.api.dtos.session.SessionDisplayPreferenceRequest;
import com.cybermanager.api.dtos.session.SessionDisplayPreferenceResponse;
import com.cybermanager.application.usecases.session.ConfigureSessionDisplayPreferenceUseCase;
import com.cybermanager.application.usecases.session.GetSessionDisplayPreferenceUseCase;
import com.cybermanager.domain.commands.session.ConfigureSessionDisplayPreferenceCommand;
import com.cybermanager.domain.queries.session.GetSessionDisplayPreferenceQuery;
import com.cybermanager.domain.valueobjects.users.EmailAddress;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions/display-preference")
public class SessionDisplayPreferenceController {

    private final GetSessionDisplayPreferenceUseCase getUseCase;
    private final ConfigureSessionDisplayPreferenceUseCase configureUseCase;
    private final JwtAccessTokenReader tokenReader;

    public SessionDisplayPreferenceController(GetSessionDisplayPreferenceUseCase getUseCase,
                                               ConfigureSessionDisplayPreferenceUseCase configureUseCase,
                                               JwtAccessTokenReader tokenReader) {
        this.getUseCase = getUseCase;
        this.configureUseCase = configureUseCase;
        this.tokenReader = tokenReader;
    }

    @GetMapping
    public ResponseEntity<SessionDisplayPreferenceResponse> get(@RequestHeader("Authorization") String authorization) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = getUseCase.execute(new GetSessionDisplayPreferenceQuery(new EmailAddress(actor.email())));
        return view == null ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(new SessionDisplayPreferenceResponse(view.groupEntries()));
    }

    @PutMapping
    public ResponseEntity<SessionDisplayPreferenceResponse> configure(
            @RequestHeader("Authorization") String authorization,
            @RequestBody SessionDisplayPreferenceRequest request
    ) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = configureUseCase.execute(new ConfigureSessionDisplayPreferenceCommand(
                new EmailAddress(actor.email()), request.groupEntries()));
        return ResponseEntity.ok(new SessionDisplayPreferenceResponse(view.groupEntries()));
    }

}
