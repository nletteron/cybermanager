package com.cybermanager.application.usecases.subscription;

import com.cybermanager.application.views.subscription.SubscriptionOfferView;
import com.cybermanager.domain.businessobjects.subscription.SubscriptionOffer;
import com.cybermanager.domain.ports.subscription.SubscriptionOfferRepository;
import com.cybermanager.domain.queries.subscription.SearchSubscriptionOffersQuery;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SearchSubscriptionOffersUseCase {

    private final SubscriptionOfferRepository repository;

    public SearchSubscriptionOffersUseCase(SubscriptionOfferRepository repository) {
        this.repository = repository;
    }


    @Transactional(readOnly = true)
    public List<SubscriptionOfferView> execute(SearchSubscriptionOffersQuery query) {
        SubscriptionOfferStatus status = query.status() == null || query.status().isBlank() ? null : SubscriptionOfferStatus.valueOf(query.status().toUpperCase());
        return repository.search(query.term(), status).stream().map(this::toView).toList();
    }


    private SubscriptionOfferView toView(SubscriptionOffer offer) {
        return new SubscriptionOfferView(offer.id().value(), offer.name(), offer.price().amount(), offer.includedMinutes(), offer.status().name());
    }

}
