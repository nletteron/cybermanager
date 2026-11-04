package com.cybermanager.api.controllers.customer;

import com.cybermanager.api.abstracts.ApiSupport;
import com.cybermanager.api.dtos.customer.*;
import com.cybermanager.api.dtos.sales.SaleLineResponse;
import com.cybermanager.api.mappers.customer.CustomerApiMapper;
import com.cybermanager.application.usecases.customer.*;
import com.cybermanager.domain.queries.customer.*;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final SearchCustomersUseCase searchCustomersUseCase;
    private final SearchCustomerArchiveCandidatesUseCase searchCustomerArchiveCandidatesUseCase;
    private final ListGeneratedArchiveFilesUseCase listGeneratedArchiveFilesUseCase;
    private final GetCustomerDetailsUseCase getCustomerDetailsUseCase;
    private final CreateCustomerUseCase createCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final ConvertCustomerToSubscriberUseCase convertCustomerToSubscriberUseCase;
    private final SearchOpenDebtsUseCase searchOpenDebtsUseCase;
    private final SettleDebtUseCase settleDebtUseCase;
    private final ReattachDebtToSessionUseCase reattachDebtToSessionUseCase;
    private final CreateDebtFromSaleUseCase createDebtFromSaleUseCase;
    private final ArchiveCustomersUseCase archiveCustomersUseCase;
    private final ExportCustomerDebtReportUseCase exportCustomerDebtReportUseCase;
    private final JwtAccessTokenReader tokenReader;
    private final CustomerApiMapper customerApiMapper;

    public CustomerController(
            SearchCustomersUseCase searchCustomersUseCase,
            SearchCustomerArchiveCandidatesUseCase searchCustomerArchiveCandidatesUseCase,
            ListGeneratedArchiveFilesUseCase listGeneratedArchiveFilesUseCase,
            GetCustomerDetailsUseCase getCustomerDetailsUseCase,
            CreateCustomerUseCase createCustomerUseCase,
            UpdateCustomerUseCase updateCustomerUseCase,
            ConvertCustomerToSubscriberUseCase convertCustomerToSubscriberUseCase,
            SearchOpenDebtsUseCase searchOpenDebtsUseCase,
            SettleDebtUseCase settleDebtUseCase,
            ReattachDebtToSessionUseCase reattachDebtToSessionUseCase,
            CreateDebtFromSaleUseCase createDebtFromSaleUseCase,
            ArchiveCustomersUseCase archiveCustomersUseCase,
            ExportCustomerDebtReportUseCase exportCustomerDebtReportUseCase,
            JwtAccessTokenReader tokenReader,
            CustomerApiMapper customerApiMapper
    ) {
        this.searchCustomersUseCase = searchCustomersUseCase;
        this.searchCustomerArchiveCandidatesUseCase = searchCustomerArchiveCandidatesUseCase;
        this.listGeneratedArchiveFilesUseCase = listGeneratedArchiveFilesUseCase;
        this.getCustomerDetailsUseCase = getCustomerDetailsUseCase;
        this.createCustomerUseCase = createCustomerUseCase;
        this.updateCustomerUseCase = updateCustomerUseCase;
        this.convertCustomerToSubscriberUseCase = convertCustomerToSubscriberUseCase;
        this.searchOpenDebtsUseCase = searchOpenDebtsUseCase;
        this.settleDebtUseCase = settleDebtUseCase;
        this.reattachDebtToSessionUseCase = reattachDebtToSessionUseCase;
        this.createDebtFromSaleUseCase = createDebtFromSaleUseCase;
        this.archiveCustomersUseCase = archiveCustomersUseCase;
        this.exportCustomerDebtReportUseCase = exportCustomerDebtReportUseCase;
        this.tokenReader = tokenReader;
        this.customerApiMapper = customerApiMapper;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> search(
            @RequestParam(name = "term", required = false) String term,
            @RequestParam(name = "type", required = false) String type
    ) {
        return ResponseEntity.ok(searchCustomersUseCase.execute(new SearchCustomersQuery(term, type)).stream()
                .map(view -> new CustomerResponse(view.customerId(), view.name(), view.type(), view.status(), view.remainingMinutes(), view.openDebtAmount()))
                .toList());
    }

    @GetMapping("/archive/candidates")
    public ResponseEntity<List<ArchiveCustomerCandidateResponse>> archiveCandidates(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @RequestParam(name = "type", required = false) String type
    ) {
        return ResponseEntity.ok(searchCustomerArchiveCandidatesUseCase.execute(new SearchCustomerArchiveCandidatesQuery(startDate, endDate, type)).stream()
                .map(candidate -> new ArchiveCustomerCandidateResponse(
                        candidate.customerId(),
                        candidate.name(),
                        candidate.type(),
                        candidate.status(),
                        candidate.remainingMinutes(),
                        candidate.latestActivityAt(),
                        candidate.sessionCount(),
                        candidate.saleCount(),
                        candidate.debtCount(),
                        candidate.salesTotal(),
                        candidate.debtTotal()
                ))
                .toList());
    }

    @GetMapping("/archive/files")
    public ResponseEntity<List<GeneratedArchiveFileResponse>> generatedArchiveFiles() {
        return ResponseEntity.ok(listGeneratedArchiveFilesUseCase.execute(new ListGeneratedArchiveFilesQuery()).stream()
                .map(file -> new GeneratedArchiveFileResponse(file.id(), file.fileName(), file.fileType(), file.generatedBy(), file.generatedAt()))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDetailsResponse> get(@PathVariable("id") UUID id) {
        var view = getCustomerDetailsUseCase.execute(new GetCustomerDetailsQuery(id));
        return ResponseEntity.ok(new CustomerDetailsResponse(
                view.customerId(),
                view.name(),
                view.type(),
                view.status(),
                view.remainingMinutes(),
                view.currentSubscriptionLabel(),
                view.purchases().stream().map(purchase -> new CustomerPurchaseResponse(
                        purchase.saleId(),
                        purchase.sessionId(),
                        purchase.type(),
                        purchase.label(),
                        purchase.debtLabel(),
                        purchase.soldAt(),
                        purchase.totalAmount(),
                        purchase.openDebt(),
                        purchase.debtId(),
                        purchase.lines().stream().map(line -> new SaleLineResponse(line.label(), line.quantity(),
                                line.unitPrice(), line.totalPrice())).toList()
                )).toList(),
                view.debts().stream().map(debt -> new CustomerDebtResponse(
                        debt.debtId(),
                        debt.label(),
                        debt.comment(),
                        debt.amount(),
                        debt.status(),
                        debt.createdAt(),
                        debt.settledAt()
                )).toList()
        ));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@RequestHeader("Authorization") String authorization, @RequestBody CustomerRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = createCustomerUseCase.execute(customerApiMapper.toCreateCustomerCommand(actor.email(), actor.roles(), request));
        return ResponseEntity.ok(new CustomerResponse(view.customerId(), view.name(), view.type(), view.status(), view.remainingMinutes(), view.openDebtAmount()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id, @RequestBody CustomerRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = updateCustomerUseCase.execute(customerApiMapper.toUpdateCustomerCommand(actor.email(), actor.roles(), id, request));
        return ResponseEntity.ok(new CustomerResponse(view.customerId(), view.name(), view.type(), view.status(), view.remainingMinutes(), view.openDebtAmount()));
    }

    @PostMapping("/{id}/convert-to-subscriber")
    public ResponseEntity<ConversionResponse> convert(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id, @RequestBody ConvertToSubscriberRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = convertCustomerToSubscriberUseCase.execute(customerApiMapper.toConvertCustomerToSubscriberCommand(actor.email(), actor.roles(), id, request));
        var customer = view.customer();
        return ResponseEntity.ok(new ConversionResponse(new CustomerResponse(customer.customerId(), customer.name(), customer.type(), customer.status(), customer.remainingMinutes(), customer.openDebtAmount()), view.saleId(), view.deductedMinutes()));
    }

    @GetMapping("/debts")
    public ResponseEntity<List<DebtCustomerResponse>> debts() {
        return ResponseEntity.ok(searchOpenDebtsUseCase.execute(new SearchOpenDebtsQuery()).stream()
                .map(customer -> new DebtCustomerResponse(
                        customer.customerId(),
                        customer.customerName(),
                        customer.customerType(),
                        customer.totalOpenDebt(),
                        customer.debts().stream().map(debt -> new CustomerDebtResponse(
                                debt.debtId(),
                                debt.label(),
                                debt.comment(),
                                debt.amount(),
                                debt.status(),
                                debt.createdAt(),
                                debt.settledAt()
                        )).toList()
                ))
                .toList());
    }

    @PostMapping("/debts/{debtId}/settle")
    public ResponseEntity<Void> settleDebt(@RequestHeader("Authorization") String authorization, @PathVariable("debtId") UUID debtId, @RequestBody(required = false) SettleDebtRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        settleDebtUseCase.execute(customerApiMapper.toSettleDebtCommand(actor.email(), actor.roles(), debtId, request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/debts/{debtId}/reattach-to-session")
    public ResponseEntity<Void> reattachDebtToSession(@RequestHeader("Authorization") String authorization, @PathVariable("debtId") UUID debtId, @RequestBody ReattachDebtToSessionRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        reattachDebtToSessionUseCase.execute(customerApiMapper.toReattachDebtToSessionCommand(actor.email(), actor.roles(), debtId, request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sales/{saleId}/create-debt")
    public ResponseEntity<Void> createDebtFromSale(@RequestHeader("Authorization") String authorization, @PathVariable("saleId") UUID saleId) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        createDebtFromSaleUseCase.execute(customerApiMapper.toCreateDebtFromSaleCommand(actor.email(), actor.roles(), saleId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/archive/export")
    public ResponseEntity<byte[]> archiveCustomers(@RequestHeader("Authorization") String authorization, @RequestBody ArchiveCustomersRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var result = archiveCustomersUseCase.execute(customerApiMapper.toArchiveCustomersCommand(actor.email(), actor.roles(), request));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .header("X-Archived-Count", String.valueOf(result.archivedCustomers()))
                .contentType(MediaType.parseMediaType(result.mediaType()))
                .body(result.content());
    }

    @GetMapping("/archive/debts-report")
    public ResponseEntity<byte[]> archiveDebtsReport(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @RequestParam(name = "type", required = false) String type
    ) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var result = exportCustomerDebtReportUseCase.execute(new ExportCustomerDebtReportQuery(actor.email(), startDate, endDate, type));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .contentType(MediaType.parseMediaType(result.mediaType()))
                .body(result.content());
    }

}
