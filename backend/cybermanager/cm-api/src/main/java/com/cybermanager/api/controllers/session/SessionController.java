package com.cybermanager.api.controllers.session;

import com.cybermanager.api.dtos.session.*;
import com.cybermanager.api.mappers.session.SessionApiMapper;
import com.cybermanager.application.usecases.session.*;
import com.cybermanager.application.views.session.SessionView;
import com.cybermanager.domain.commands.session.DeleteSessionEntryCommand;
import com.cybermanager.domain.queries.session.SearchSessionsOfDayQuery;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SearchSessionsOfDayUseCase searchSessionsOfDayUseCase;
    private final GetCurrentSessionsUseCase getCurrentSessionsUseCase;
    private final StartSessionUseCase startSessionUseCase;
    private final StopSessionUseCase stopSessionUseCase;
    private final PaySessionUseCase paySessionUseCase;
    private final PaySessionAndCreateInvoiceUseCase paySessionAndCreateInvoiceUseCase;
    private final PauseSessionUseCase pauseSessionUseCase;
    private final ResumeSessionUseCase resumeSessionUseCase;
    private final RestartSessionsDayUseCase restartSessionsDayUseCase;
    private final SessionApiMapper sessionApiMapper;
    private final DeleteSessionEntryUseCase deleteSessionEntryUseCase;

    public SessionController(
            SearchSessionsOfDayUseCase searchSessionsOfDayUseCase,
            GetCurrentSessionsUseCase getCurrentSessionsUseCase,
            StartSessionUseCase startSessionUseCase,
            StopSessionUseCase stopSessionUseCase,
            PaySessionUseCase paySessionUseCase,
            PaySessionAndCreateInvoiceUseCase paySessionAndCreateInvoiceUseCase,
            PauseSessionUseCase pauseSessionUseCase,
            ResumeSessionUseCase resumeSessionUseCase,
            RestartSessionsDayUseCase restartSessionsDayUseCase,
            SessionApiMapper sessionApiMapper,
            DeleteSessionEntryUseCase deleteSessionEntryUseCase
    ) {
        this.searchSessionsOfDayUseCase = searchSessionsOfDayUseCase;
        this.getCurrentSessionsUseCase = getCurrentSessionsUseCase;
        this.startSessionUseCase = startSessionUseCase;
        this.stopSessionUseCase = stopSessionUseCase;
        this.paySessionUseCase = paySessionUseCase;
        this.paySessionAndCreateInvoiceUseCase = paySessionAndCreateInvoiceUseCase;
        this.pauseSessionUseCase = pauseSessionUseCase;
        this.resumeSessionUseCase = resumeSessionUseCase;
        this.restartSessionsDayUseCase = restartSessionsDayUseCase;
        this.sessionApiMapper = sessionApiMapper;
        this.deleteSessionEntryUseCase = deleteSessionEntryUseCase;
    }

    @DeleteMapping("/{id}/purchases/{saleId}")
    public ResponseEntity<Void> deletePurchase(@PathVariable("id") UUID id, @PathVariable("saleId") UUID saleId) {
        deleteSessionEntryUseCase.execute(new DeleteSessionEntryCommand(id, saleId, null));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/debts/{debtId}")
    public ResponseEntity<Void> deleteDebt(@PathVariable("id") UUID id, @PathVariable("debtId") UUID debtId) {
        deleteSessionEntryUseCase.execute(new DeleteSessionEntryCommand(id, null, debtId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/day")
    public ResponseEntity<List<SessionResponse>> day(@RequestParam(name = "date", required = false) String date) {
        LocalDate parsed = date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
        return ResponseEntity.ok(searchSessionsOfDayUseCase.execute(new SearchSessionsOfDayQuery(parsed)).stream().map(this::toResponse).toList());
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentSessionsResponse> current() {
        return ResponseEntity.ok(new CurrentSessionsResponse(getCurrentSessionsUseCase.execute().sessions().stream().map(this::toResponse).toList()));
    }

    @PostMapping("/start")
    public ResponseEntity<SessionResponse> start(@RequestBody StartSessionRequest request) {
        return ResponseEntity.ok(toResponse(startSessionUseCase.execute(sessionApiMapper.toStartSessionCommand(request))));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<SessionResponse> stop(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(toResponse(stopSessionUseCase.execute(sessionApiMapper.toStopSessionCommand(id))));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<SessionResponse> pay(@PathVariable("id") UUID id, @RequestBody(required = false) PaySessionRequest request) {
        return ResponseEntity.ok(toResponse(paySessionUseCase.execute(sessionApiMapper.toPaySessionCommand(id, request))));
    }

    @PostMapping("/{id}/pay-with-invoice")
    public ResponseEntity<byte[]> payWithInvoice(@PathVariable("id") UUID id, @RequestBody(required = false) PaySessionRequest request) {
        var result = paySessionAndCreateInvoiceUseCase.execute(sessionApiMapper.toPaySessionAndCreateInvoiceCommand(id, request));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .header("X-Invoice-Id", result.invoiceId().toString())
                .header("X-Invoice-Number", result.invoiceNumber())
                .contentType(MediaType.parseMediaType(result.mediaType()))
                .body(result.content());
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<SessionResponse> pause(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(toResponse(pauseSessionUseCase.execute(sessionApiMapper.toPauseSessionCommand(id))));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<SessionResponse> resume(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(toResponse(resumeSessionUseCase.execute(sessionApiMapper.toResumeSessionCommand(id))));
    }

    @PostMapping("/restart-day")
    public ResponseEntity<RestartSessionsDayResponse> restartDay() {
        return ResponseEntity.ok(new RestartSessionsDayResponse(restartSessionsDayUseCase.execute(sessionApiMapper.toRestartSessionsDayCommand())));
    }

    private SessionResponse toResponse(SessionView view) {
        return new SessionResponse(
                view.sessionId(),
                view.customerId(),
                view.customerName(),
                view.customerType(),
                view.remainingMinutes(),
                view.displayRemainingMinutes(),
                view.workstation(),
                view.startedAt(),
                view.endedAt(),
                view.paused(),
                view.paid(),
                view.consumedSeconds(),
                view.consumedMinutes(),
                view.calculatedPrice(),
                view.purchasesAmount(),
                view.openDebtAmount(),
                view.totalAmountDue(),
                view.totalPaidAmount()
        );
    }

}
