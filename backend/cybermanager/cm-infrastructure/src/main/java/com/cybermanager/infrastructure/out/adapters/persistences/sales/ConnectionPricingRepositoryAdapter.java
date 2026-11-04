package com.cybermanager.infrastructure.out.adapters.persistences.sales;

import com.cybermanager.domain.businessobjects.sales.ConnectionPricingRule;
import com.cybermanager.domain.ports.sales.ConnectionPricingRepository;
import com.cybermanager.domain.valueobjects.sales.ConnectionPricingTier;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.ConnectionPricingGridJpaRepository;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.ConnectionPricingJpaRepository;
import com.cybermanager.infrastructure.out.entities.sales.ConnectionPricingGridJpaEntity;
import com.cybermanager.infrastructure.out.entities.sales.ConnectionPricingTierJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ConnectionPricingRepositoryAdapter implements ConnectionPricingRepository {

    private final ConnectionPricingGridJpaRepository gridRepository;
    private final ConnectionPricingJpaRepository tierRepository;

    public ConnectionPricingRepositoryAdapter(
            ConnectionPricingGridJpaRepository gridRepository,
            ConnectionPricingJpaRepository tierRepository
    ) {
        this.gridRepository = gridRepository;
        this.tierRepository = tierRepository;
    }

    @Override
    public ConnectionPricingRule save(ConnectionPricingRule rule) {
        LocalDateTime now = LocalDateTime.now();
        gridRepository.findFirstByActiveTrue().ifPresent(current -> {
            current.active = false;
            current.updatedAt = now;
            gridRepository.save(current);
            gridRepository.flush();
        });

        ConnectionPricingGridJpaEntity grid = new ConnectionPricingGridJpaEntity();
        grid.active = true;
        grid.createdAt = now;
        grid.updatedAt = now;
        ConnectionPricingGridJpaEntity savedGrid = gridRepository.save(grid);

        List<ConnectionPricingTierJpaEntity> entities = rule.tiers().stream()
                .map(tier -> toEntity(tier, savedGrid, now))
                .toList();

        List<ConnectionPricingTier> savedTiers = tierRepository.saveAll(entities).stream()
                .map(this::toDomain)
                .toList();
        return new ConnectionPricingRule(savedTiers);
    }

    private ConnectionPricingTierJpaEntity toEntity(ConnectionPricingTier tier, ConnectionPricingGridJpaEntity grid, LocalDateTime now) {
        ConnectionPricingTierJpaEntity entity = new ConnectionPricingTierJpaEntity();
        entity.grid = grid;
        entity.durationMinutes = tier.durationMinutes();
        entity.price = tier.price().amount();
        entity.createdAt = now;
        entity.updatedAt = now;

        return entity;
    }

    @Override
    public ConnectionPricingRule getCurrentRule() {
        return gridRepository.findFirstByActiveTrue()
                .map(grid -> new ConnectionPricingRule(
                        tierRepository.findAllByGridIdOrderByDurationMinutesAsc(grid.id).stream()
                                .map(this::toDomain)
                                .toList()
                ))
                .orElseGet(() -> new ConnectionPricingRule(List.of()));
    }

    private ConnectionPricingTier toDomain(ConnectionPricingTierJpaEntity entity) {
        return new ConnectionPricingTier(
                entity.id,
                entity.durationMinutes,
                new Money(entity.price),
                entity.createdAt,
                entity.updatedAt
        );
    }

}


