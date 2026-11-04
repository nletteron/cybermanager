package com.cybermanager.application.usecases.subscription;

import com.cybermanager.application.views.subscription.SubscriptionOfferView;
import com.cybermanager.domain.businessobjects.subscription.SubscriptionOffer;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.queries.subscription.GetSubscriptionOfferDetailsQuery;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GetSubscriptionOfferDetailsUseCase {

    private final SubscriptionOfferRepository repository;

    public GetSubscriptionOfferDetailsUseCase(SubscriptionOfferRepository repository) {
        this.repository = repository;
    }


    @Transactional(readOnly = true)
    public SubscriptionOfferView execute(GetSubscriptionOfferDetailsQuery query) {
        return repository.findById(new SubscriptionOfferId(query.offerId())).map(this::toView)
                .orElseThrow(() -> new IllegalArgumentException("Subscription offer not found"));
    }


    private SubscriptionOfferView toView(SubscriptionOffer offer) {
        return new SubscriptionOfferView(offer.id().value(), offer.name(), offer.price().amount(), offer.includedMinutes(), offer.status().name());
    }

}
