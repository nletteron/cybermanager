package com.cybermanager.api.controllers.sales;

import com.cybermanager.api.abstracts.ApiSupport;
import com.cybermanager.api.dtos.sales.*;
import com.cybermanager.api.mappers.sales.SalesApiMapper;
import com.cybermanager.application.usecases.sales.*;
import com.cybermanager.application.views.sales.SaleView;
import com.cybermanager.domain.queries.sales.GetConnectionPricingQuery;
import com.cybermanager.domain.queries.sales.GetSaleDetailsQuery;
import com.cybermanager.domain.queries.sales.SearchSalesOfDayQuery;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class SalesController {

    private final SearchSalesOfDayUseCase searchSalesOfDayUseCase;
    private final GetSaleDetailsUseCase getSaleDetailsUseCase;
    private final CreateProductSaleUseCase createProductSaleUseCase;
    private final CreateSubscriptionSaleUseCase createSubscriptionSaleUseCase;
    private final DeleteSubscriptionSaleUseCase deleteSubscriptionSaleUseCase;
    private final CreateConnectionTimeSaleUseCase createConnectionTimeSaleUseCase;
    private final ConfigureConnectionPricingUseCase configureConnectionPricingUseCase;
    private final GetConnectionPricingUseCase getConnectionPricingUseCase;
    private final JwtAccessTokenReader tokenReader;
    private final SalesApiMapper salesApiMapper;

    public SalesController(
            SearchSalesOfDayUseCase searchSalesOfDayUseCase,
            GetSaleDetailsUseCase getSaleDetailsUseCase,
            CreateProductSaleUseCase createProductSaleUseCase,
            CreateSubscriptionSaleUseCase createSubscriptionSaleUseCase,
            DeleteSubscriptionSaleUseCase deleteSubscriptionSaleUseCase,
            CreateConnectionTimeSaleUseCase createConnectionTimeSaleUseCase,
            ConfigureConnectionPricingUseCase configureConnectionPricingUseCase,
            GetConnectionPricingUseCase getConnectionPricingUseCase,
            JwtAccessTokenReader tokenReader,
            SalesApiMapper salesApiMapper
    ) {
        this.searchSalesOfDayUseCase = searchSalesOfDayUseCase;
        this.getSaleDetailsUseCase = getSaleDetailsUseCase;
        this.createProductSaleUseCase = createProductSaleUseCase;
        this.createSubscriptionSaleUseCase = createSubscriptionSaleUseCase;
        this.deleteSubscriptionSaleUseCase = deleteSubscriptionSaleUseCase;
        this.createConnectionTimeSaleUseCase = createConnectionTimeSaleUseCase;
        this.configureConnectionPricingUseCase = configureConnectionPricingUseCase;
        this.getConnectionPricingUseCase = getConnectionPricingUseCase;
        this.tokenReader = tokenReader;
        this.salesApiMapper = salesApiMapper;
    }

    @GetMapping("/api/sales/day")
    public ResponseEntity<List<SaleResponse>> day(@RequestParam(name = "date", required = false) String date) {
        LocalDate parsed = date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
        return ResponseEntity.ok(searchSalesOfDayUseCase.execute(new SearchSalesOfDayQuery(parsed)).stream().map(this::toResponse).toList());
    }

    @GetMapping("/api/sales/{id}")
    public ResponseEntity<SaleResponse> get(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(toResponse(getSaleDetailsUseCase.execute(new GetSaleDetailsQuery(id))));
    }

    @PostMapping("/api/sales/products")
    public ResponseEntity<SaleResponse> productSale(@RequestHeader("Authorization") String authorization, @RequestBody ProductSaleRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = createProductSaleUseCase.execute(salesApiMapper.toCreateProductSaleCommand(actor.email(), actor.roles(), request));
        return ResponseEntity.ok(toResponse(view));
    }

    @PostMapping("/api/sales/subscriptions")
    public ResponseEntity<SaleResponse> subscriptionSale(@RequestHeader("Authorization") String authorization, @RequestBody SubscriptionSaleRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        return ResponseEntity.ok(toResponse(createSubscriptionSaleUseCase.execute(salesApiMapper.toCreateSubscriptionSaleCommand(actor.email(), actor.roles(), request))));
    }

    @DeleteMapping("/api/sales/{id}/subscription-session")
    public ResponseEntity<Void> deleteSubscriptionSale(@PathVariable("id") UUID id) {
        deleteSubscriptionSaleUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/sales/connection-time")
    public ResponseEntity<SaleResponse> connectionSale(@RequestHeader("Authorization") String authorization, @RequestBody ConnectionTimeSaleRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        return ResponseEntity.ok(toResponse(createConnectionTimeSaleUseCase.execute(salesApiMapper.toCreateConnectionTimeSaleCommand(actor.email(), actor.roles(), request))));
    }

    @PutMapping("/api/pricing/connection-time")
    public ResponseEntity<PricingResponse> configurePricing(@RequestHeader("Authorization") String authorization, @RequestBody PricingRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = configureConnectionPricingUseCase.execute(salesApiMapper.toConfigureConnectionPricingCommand(actor.email(), actor.roles(), request));
        return ResponseEntity.ok(toPricingResponse(view));
    }

    @GetMapping("/api/pricing/connection-time")
    public ResponseEntity<PricingResponse> getPricing() {
        var view = getConnectionPricingUseCase.execute(new GetConnectionPricingQuery());
        return ResponseEntity.ok(toPricingResponse(view));
    }

    private SaleResponse toResponse(SaleView view) {
        return new SaleResponse(view.saleId(), view.customerId(), view.type(), view.soldAt(), view.lines().stream().map(line -> new SaleLineResponse(line.label(), line.quantity(), line.unitPrice(), line.totalPrice())).toList(), view.totalAmount());
    }

    private PricingResponse toPricingResponse(com.cybermanager.application.views.sales.ConnectionPricingView view) {
        return new PricingResponse(view.tiers().stream()
                .map(tier -> new PricingTierResponse(tier.id(), tier.hours(), tier.minutes(), tier.durationMinutes(), tier.price(), tier.createdAt(), tier.updatedAt()))
                .toList());
    }

}

