package com.cybermanager.application.usecases.subscription;

import com.cybermanager.application.services.shared.ActorSupport;
import com.cybermanager.application.views.subscription.SubscriptionOfferView;
import com.cybermanager.domain.businessobjects.subscription.SubscriptionOffer;
import com.cybermanager.domain.commands.subscription.CreateSubscriptionOfferCommand;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.valueobjects.shared.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateSubscriptionOfferUseCase {

    private final SubscriptionOfferRepository repository;

    public CreateSubscriptionOfferUseCase(SubscriptionOfferRepository repository) {
        this.repository = repository;
    }


    public SubscriptionOfferView execute(CreateSubscriptionOfferCommand command) {
        ActorSupport.requireAdmin(command.actorRoles());
        return toView(repository.save(SubscriptionOffer.create(command.name(), new Money(command.price()), command.includedMinutes())));
    }


    private SubscriptionOfferView toView(SubscriptionOffer offer) {
        return new SubscriptionOfferView(offer.id().value(), offer.name(), offer.price().amount(), offer.includedMinutes(), offer.status().name());
    }

}
