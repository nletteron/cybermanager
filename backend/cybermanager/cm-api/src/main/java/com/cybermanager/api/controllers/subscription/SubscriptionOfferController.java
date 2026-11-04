package com.cybermanager.api.controllers.subscription;

import com.cybermanager.api.abstracts.ApiSupport;
import com.cybermanager.api.dtos.subscription.SubscriptionOfferRequest;
import com.cybermanager.api.dtos.subscription.SubscriptionOfferResponse;
import com.cybermanager.api.mappers.subscription.SubscriptionOfferApiMapper;
import com.cybermanager.application.usecases.subscription.*;
import com.cybermanager.domain.queries.subscription.GetSubscriptionOfferDetailsQuery;
import com.cybermanager.domain.queries.subscription.SearchSubscriptionOffersQuery;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscription-offers")
public class SubscriptionOfferController {

    private final SearchSubscriptionOffersUseCase searchSubscriptionOffersUseCase;
    private final GetSubscriptionOfferDetailsUseCase getSubscriptionOfferDetailsUseCase;
    private final CreateSubscriptionOfferUseCase createSubscriptionOfferUseCase;
    private final UpdateSubscriptionOfferUseCase updateSubscriptionOfferUseCase;
    private final ActivateSubscriptionOfferUseCase activateSubscriptionOfferUseCase;
    private final DeactivateSubscriptionOfferUseCase deactivateSubscriptionOfferUseCase;
    private final DeleteSubscriptionOfferUseCase deleteSubscriptionOfferUseCase;
    private final JwtAccessTokenReader tokenReader;
    private final SubscriptionOfferApiMapper subscriptionOfferApiMapper;

    public SubscriptionOfferController(
            SearchSubscriptionOffersUseCase searchSubscriptionOffersUseCase,
            GetSubscriptionOfferDetailsUseCase getSubscriptionOfferDetailsUseCase,
            CreateSubscriptionOfferUseCase createSubscriptionOfferUseCase,
            UpdateSubscriptionOfferUseCase updateSubscriptionOfferUseCase,
            ActivateSubscriptionOfferUseCase activateSubscriptionOfferUseCase,
            DeactivateSubscriptionOfferUseCase deactivateSubscriptionOfferUseCase,
            DeleteSubscriptionOfferUseCase deleteSubscriptionOfferUseCase,
            JwtAccessTokenReader tokenReader,
            SubscriptionOfferApiMapper subscriptionOfferApiMapper
    ) {
        this.searchSubscriptionOffersUseCase = searchSubscriptionOffersUseCase;
        this.getSubscriptionOfferDetailsUseCase = getSubscriptionOfferDetailsUseCase;
        this.createSubscriptionOfferUseCase = createSubscriptionOfferUseCase;
        this.updateSubscriptionOfferUseCase = updateSubscriptionOfferUseCase;
        this.activateSubscriptionOfferUseCase = activateSubscriptionOfferUseCase;
        this.deactivateSubscriptionOfferUseCase = deactivateSubscriptionOfferUseCase;
        this.deleteSubscriptionOfferUseCase = deleteSubscriptionOfferUseCase;
        this.tokenReader = tokenReader;
        this.subscriptionOfferApiMapper = subscriptionOfferApiMapper;
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionOfferResponse>> search(
            @RequestParam(name = "term", required = false) String term,
            @RequestParam(name = "status", required = false) String status
    ) {
        return ResponseEntity.ok(searchSubscriptionOffersUseCase.execute(new SearchSubscriptionOffersQuery(term, status)).stream().map(view -> new SubscriptionOfferResponse(view.offerId(), view.name(), view.price(), view.includedMinutes(), view.status())).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionOfferResponse> get(@PathVariable("id") UUID id) {
        var view = getSubscriptionOfferDetailsUseCase.execute(new GetSubscriptionOfferDetailsQuery(id));
        return ResponseEntity.ok(new SubscriptionOfferResponse(view.offerId(), view.name(), view.price(), view.includedMinutes(), view.status()));
    }

    @PostMapping
    public ResponseEntity<SubscriptionOfferResponse> create(@RequestHeader("Authorization") String authorization, @RequestBody SubscriptionOfferRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = createSubscriptionOfferUseCase.execute(subscriptionOfferApiMapper.toCreateSubscriptionOfferCommand(actor.email(), actor.roles(), request));
        return ResponseEntity.ok(new SubscriptionOfferResponse(view.offerId(), view.name(), view.price(), view.includedMinutes(), view.status()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionOfferResponse> update(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id, @RequestBody SubscriptionOfferRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = updateSubscriptionOfferUseCase.execute(subscriptionOfferApiMapper.toUpdateSubscriptionOfferCommand(actor.email(), actor.roles(), id, request));
        return ResponseEntity.ok(new SubscriptionOfferResponse(view.offerId(), view.name(), view.price(), view.includedMinutes(), view.status()));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<SubscriptionOfferResponse> activate(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = activateSubscriptionOfferUseCase.execute(subscriptionOfferApiMapper.toActivateSubscriptionOfferCommand(actor.email(), actor.roles(), id));
        return ResponseEntity.ok(new SubscriptionOfferResponse(view.offerId(), view.name(), view.price(), view.includedMinutes(), view.status()));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<SubscriptionOfferResponse> deactivate(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = deactivateSubscriptionOfferUseCase.execute(subscriptionOfferApiMapper.toDeactivateSubscriptionOfferCommand(actor.email(), actor.roles(), id));
        return ResponseEntity.ok(new SubscriptionOfferResponse(view.offerId(), view.name(), view.price(), view.includedMinutes(), view.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        deleteSubscriptionOfferUseCase.execute(subscriptionOfferApiMapper.toDeleteSubscriptionOfferCommand(actor.email(), actor.roles(), id));
        return ResponseEntity.noContent().build();
    }

}

