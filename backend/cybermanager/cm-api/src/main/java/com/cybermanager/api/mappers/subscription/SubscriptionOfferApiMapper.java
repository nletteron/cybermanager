package com.cybermanager.api.mappers.subscription;

import com.cybermanager.api.dtos.subscription.SubscriptionOfferRequest;
import com.cybermanager.domain.commands.subscription.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class SubscriptionOfferApiMapper {

    public CreateSubscriptionOfferCommand toCreateSubscriptionOfferCommand(String actorEmail, Set<String> actorRoles, SubscriptionOfferRequest request) {
        return new CreateSubscriptionOfferCommand(actorEmail, actorRoles, request.name(), request.price(), request.includedMinutes());
    }

    public UpdateSubscriptionOfferCommand toUpdateSubscriptionOfferCommand(String actorEmail, Set<String> actorRoles, UUID offerId, SubscriptionOfferRequest request) {
        return new UpdateSubscriptionOfferCommand(actorEmail, actorRoles, offerId, request.name(), request.price(), request.includedMinutes());
    }

    public ActivateSubscriptionOfferCommand toActivateSubscriptionOfferCommand(String actorEmail, Set<String> actorRoles, UUID offerId) {
        return new ActivateSubscriptionOfferCommand(actorEmail, actorRoles, offerId);
    }

    public DeactivateSubscriptionOfferCommand toDeactivateSubscriptionOfferCommand(String actorEmail, Set<String> actorRoles, UUID offerId) {
        return new DeactivateSubscriptionOfferCommand(actorEmail, actorRoles, offerId);
    }

    public DeleteSubscriptionOfferCommand toDeleteSubscriptionOfferCommand(String actorEmail, Set<String> actorRoles, UUID offerId) {
        return new DeleteSubscriptionOfferCommand(actorEmail, actorRoles, offerId);
    }

}
