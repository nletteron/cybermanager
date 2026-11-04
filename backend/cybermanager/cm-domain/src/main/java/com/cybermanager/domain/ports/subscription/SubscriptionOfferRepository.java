package com.cybermanager.domain.ports.subscription;

import com.cybermanager.domain.businessobjects.subscription.SubscriptionOffer;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferId;
import com.cybermanager.domain.valueobjects.subscription.SubscriptionOfferStatus;

import java.util.List;
import java.util.Optional;

public interface SubscriptionOfferRepository {

    SubscriptionOffer save(SubscriptionOffer offer);

    Optional<SubscriptionOffer> findById(SubscriptionOfferId offerId);

    void deleteById(SubscriptionOfferId offerId);

    List<SubscriptionOffer> search(String term, SubscriptionOfferStatus status);

}

