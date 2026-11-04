package com.cybermanager.application.usecases.subscription;

import com.cybermanager.application.services.shared.ActorSupport;
import com.cybermanager.application.views.subscription.SubscriptionOfferView;
import com.cybermanager.domain.businessobjects.subscription.SubscriptionOffer;
import com.cybermanager.domain.commands.subscription.DeleteSubscriptionOfferCommand;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteSubscriptionOfferUseCase {

    private final SubscriptionOfferRepository repository;

    public DeleteSubscriptionOfferUseCase(SubscriptionOfferRepository repository) {
        this.repository = repository;
    }


    public void execute(DeleteSubscriptionOfferCommand command) {
        ActorSupport.requireAdmin(command.actorRoles());
        var offerId = new SubscriptionOfferId(command.offerId());
        repository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription offer not found"));
        repository.deleteById(offerId);
    }


    private SubscriptionOfferView toView(SubscriptionOffer offer) {
        return new SubscriptionOfferView(offer.id().value(), offer.name(), offer.price().amount(), offer.includedMinutes(), offer.status().name());
    }

}
