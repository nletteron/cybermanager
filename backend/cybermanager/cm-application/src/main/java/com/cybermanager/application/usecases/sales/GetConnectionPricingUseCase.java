package com.cybermanager.application.usecases.sales;

import com.cybermanager.application.views.sales.ConnectionPricingView;
import com.cybermanager.application.views.sales.PricingTierView;
import com.cybermanager.domain.ports.sales.ConnectionPricingRepository;
import com.cybermanager.domain.queries.sales.GetConnectionPricingQuery;
import com.cybermanager.domain.valueobjects.sales.ConnectionPricingTier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetConnectionPricingUseCase {

    private final ConnectionPricingRepository pricingRepository;

    public GetConnectionPricingUseCase(ConnectionPricingRepository pricingRepository) {
        this.pricingRepository = pricingRepository;
    }

    public ConnectionPricingView execute(GetConnectionPricingQuery query) {
        return new ConnectionPricingView(pricingRepository.getCurrentRule().tiers().stream()
                .map(this::toPricingTierView)
                .toList());
    }

    private PricingTierView toPricingTierView(ConnectionPricingTier tier) {
        return new PricingTierView(
                tier.id(),
                tier.durationMinutes() / 60,
                tier.durationMinutes() % 60,
                tier.durationMinutes(),
                tier.price().amount(),
                tier.createdAt(),
                tier.updatedAt()
        );
    }

}
