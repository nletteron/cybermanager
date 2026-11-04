package com.cybermanager.api.controllers.sales;

import com.cybermanager.api.abstracts.ApiSupport;
import com.cybermanager.api.dtos.sales.CreateInvoiceRequest;
import com.cybermanager.api.dtos.sales.InvoiceDetailResponse;
import com.cybermanager.api.dtos.sales.InvoiceLineResponse;
import com.cybermanager.api.dtos.sales.InvoiceSummaryResponse;
import com.cybermanager.api.mappers.sales.SalesApiMapper;
import com.cybermanager.application.usecases.sales.*;
import com.cybermanager.application.views.sales.InvoiceDetailView;
import com.cybermanager.application.views.sales.InvoiceSummaryView;
import com.cybermanager.domain.queries.sales.GetInvoiceDetailQuery;
import com.cybermanager.domain.queries.sales.ReissueInvoicePdfQuery;
import com.cybermanager.domain.queries.sales.SearchInvoicesQuery;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final SearchInvoicesUseCase searchInvoicesUseCase;
    private final GetInvoiceDetailUseCase getInvoiceDetailUseCase;
    private final CreateManualInvoiceUseCase createManualInvoiceUseCase;
    private final CancelInvoiceUseCase cancelInvoiceUseCase;
    private final ReissueInvoicePdfUseCase reissueInvoicePdfUseCase;
    private final JwtAccessTokenReader tokenReader;
    private final SalesApiMapper salesApiMapper;

    public InvoiceController(
            SearchInvoicesUseCase searchInvoicesUseCase,
            GetInvoiceDetailUseCase getInvoiceDetailUseCase,
            CreateManualInvoiceUseCase createManualInvoiceUseCase,
            CancelInvoiceUseCase cancelInvoiceUseCase,
            ReissueInvoicePdfUseCase reissueInvoicePdfUseCase,
            JwtAccessTokenReader tokenReader,
            SalesApiMapper salesApiMapper
    ) {
        this.searchInvoicesUseCase = searchInvoicesUseCase;
        this.getInvoiceDetailUseCase = getInvoiceDetailUseCase;
        this.createManualInvoiceUseCase = createManualInvoiceUseCase;
        this.cancelInvoiceUseCase = cancelInvoiceUseCase;
        this.reissueInvoicePdfUseCase = reissueInvoicePdfUseCase;
        this.tokenReader = tokenReader;
        this.salesApiMapper = salesApiMapper;
    }

    @GetMapping
    public ResponseEntity<List<InvoiceSummaryResponse>> search(
            @RequestParam(name = "invoiceNumber", required = false) String invoiceNumber,
            @RequestParam(name = "customerName", required = false) String customerName,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) LocalDate endDate
    ) {
        return ResponseEntity.ok(searchInvoicesUseCase.execute(new SearchInvoicesQuery(invoiceNumber, customerName, status, startDate, endDate)).stream()
                .map(this::toSummaryResponse)
                .toList());
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceDetailResponse> detail(@PathVariable("invoiceId") UUID invoiceId) {
        return ResponseEntity.ok(toDetailResponse(getInvoiceDetailUseCase.execute(new GetInvoiceDetailQuery(invoiceId))));
    }

    @PostMapping
    public ResponseEntity<byte[]> create(@RequestHeader("Authorization") String authorization, @RequestBody CreateInvoiceRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var result = createManualInvoiceUseCase.execute(salesApiMapper.toCreateManualInvoiceCommand(actor.email(), request));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .header("X-Invoice-Id", result.invoiceId().toString())
                .header("X-Invoice-Number", result.invoiceNumber())
                .contentType(MediaType.parseMediaType(result.mediaType()))
                .body(result.content());
    }

    @PostMapping("/{invoiceId}/cancel")
    public ResponseEntity<InvoiceDetailResponse> cancel(@PathVariable("invoiceId") UUID invoiceId) {
        return ResponseEntity.ok(toDetailResponse(cancelInvoiceUseCase.execute(salesApiMapper.toCancelInvoiceCommand(invoiceId))));
    }

    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable("invoiceId") UUID invoiceId) {
        var result = reissueInvoicePdfUseCase.execute(new ReissueInvoicePdfQuery(invoiceId));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .header("X-Invoice-Id", result.invoiceId().toString())
                .header("X-Invoice-Number", result.invoiceNumber())
                .contentType(MediaType.parseMediaType(result.mediaType()))
                .body(result.content());
    }

    private InvoiceSummaryResponse toSummaryResponse(InvoiceSummaryView view) {
        return new InvoiceSummaryResponse(
                view.invoiceId(),
                view.saleId(),
                view.invoiceNumber(),
                view.customerName(),
                view.issuedAt(),
                view.status(),
                view.totalAmount()
        );
    }

    private InvoiceDetailResponse toDetailResponse(InvoiceDetailView view) {
        return new InvoiceDetailResponse(
                view.invoiceId(),
                view.saleId(),
                view.customerId(),
                view.invoiceNumber(),
                view.customerName(),
                view.issuedAt(),
                view.status(),
                view.totalAmount(),
                view.lines().stream()
                        .map(line -> new InvoiceLineResponse(line.label(), line.quantity(), line.unitPrice(), line.totalPrice()))
                        .toList()
        );
    }

}


