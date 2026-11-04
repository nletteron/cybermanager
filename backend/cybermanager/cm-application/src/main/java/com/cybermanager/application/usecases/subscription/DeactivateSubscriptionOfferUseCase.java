package com.cybermanager.application.usecases.subscription;

import com.cybermanager.application.services.shared.ActorSupport;
import com.cybermanager.application.views.subscription.SubscriptionOfferView;
import com.cybermanager.domain.businessobjects.subscription.SubscriptionOffer;
import com.cybermanager.domain.commands.subscription.DeactivateSubscriptionOfferCommand;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeactivateSubscriptionOfferUseCase {

    private final SubscriptionOfferRepository repository;

    public DeactivateSubscriptionOfferUseCase(SubscriptionOfferRepository repository) {
        this.repository = repository;
    }


    public SubscriptionOfferView execute(DeactivateSubscriptionOfferCommand command) {
        ActorSupport.requireAdmin(command.actorRoles());
        var offer = repository.findById(new SubscriptionOfferId(command.offerId()))
                .orElseThrow(() -> new IllegalArgumentException("Subscription offer not found"));
        return toView(repository.save(offer.deactivate()));
    }


    private SubscriptionOfferView toView(SubscriptionOffer offer) {
        return new SubscriptionOfferView(offer.id().value(), offer.name(), offer.price().amount(), offer.includedMinutes(), offer.status().name());
    }

}
