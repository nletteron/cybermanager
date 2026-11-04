package com.cybermanager.infrastructure.out.adapters.queries.reporting;

import com.cybermanager.application.ports.reporting.DayHistoryQuery;
import com.cybermanager.application.views.reporting.CustomerDayHistoryView;
import com.cybermanager.application.views.reporting.DayCustomerHistoryView;
import com.cybermanager.application.views.reporting.DayHistoryView;
import com.cybermanager.application.views.reporting.SaleActivityView;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer.CustomerJpaRepository;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer.DebtJpaRepository;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.SaleJpaRepository;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.session.CafeSessionJpaRepository;
import com.cybermanager.infrastructure.out.entities.customer.CustomerJpaEntity;
import com.cybermanager.infrastructure.out.entities.customer.DebtJpaEntity;
import com.cybermanager.infrastructure.out.entities.sales.SaleJpaEntity;
import com.cybermanager.infrastructure.out.entities.sales.SaleLineJpaEntity;
import com.cybermanager.infrastructure.out.entities.session.CafeSessionJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
public class ReportingQueryAdapter implements DayHistoryQuery {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CustomerJpaRepository customerRepository;
    private final SaleJpaRepository saleRepository;
    private final CafeSessionJpaRepository sessionRepository;
    private final DebtJpaRepository debtRepository;

    public ReportingQueryAdapter(
            CustomerJpaRepository customerRepository,
            SaleJpaRepository saleRepository,
            CafeSessionJpaRepository sessionRepository,
            DebtJpaRepository debtRepository
    ) {
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
        this.sessionRepository = sessionRepository;
        this.debtRepository = debtRepository;
    }

    @Override
    public DayHistoryView getDayHistory(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        var sales = saleRepository.findBySoldAtBetween(start, end);
        var sessions = sessionRepository.findByStartedAtBetween(start, end);
        var openDebts = debtRepository.findByStatus("OPEN");

        BigDecimal totalCollected = sales.stream().map(sale -> sale.totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        var customers = customerRepository.findAll().stream()
                .map(customer -> toDayCustomerHistoryView(customer, sales, sessions, openDebts))
                .filter(item -> item.salesTotal().compareTo(BigDecimal.ZERO) > 0
                        || item.totalMinutes() > 0
                        || item.debtTotal().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(DayCustomerHistoryView::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        BigDecimal totalDebtCreated = customers.stream()
                .map(DayCustomerHistoryView::debtTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DayHistoryView(startDate, endDate, customers, totalCollected, totalDebtCreated);
    }

    private DayCustomerHistoryView toDayCustomerHistoryView(
            CustomerJpaEntity customer,
            List<SaleJpaEntity> sales,
            List<CafeSessionJpaEntity> sessions,
            List<DebtJpaEntity> openDebts
    ) {
        var customerSessions = sessions.stream().filter(session -> session.customerId.equals(customer.id)).toList();
        var totalSales = sales.stream()
                .filter(sale -> sale.customerId.equals(customer.id))
                .map(sale -> sale.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var customerDebtTotal = openDebts.stream()
                .filter(debt -> debt.customerId.equals(customer.id))
                .map(debt -> debt.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DayCustomerHistoryView(
                customer.id,
                customer.name,
                customer.type,
                customerSessions.stream().mapToInt(this::resolveConsumedMinutes).sum(),
                totalSales,
                customerDebtTotal,
                totalSales,
                resolveSessionState(customerSessions)
        );
    }

    @Override
    public CustomerDayHistoryView getCustomerDayHistory(LocalDate startDate, LocalDate endDate, UUID customerId) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        var customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        var sales = saleRepository.findBySoldAtBetween(start, end).stream()
                .filter(sale -> sale.customerId.equals(customerId))
                .toList();
        var sessions = sessionRepository.findByStartedAtBetween(start, end).stream()
                .filter(session -> session.customerId.equals(customerId))
                .toList();
        var debts = debtRepository.findByStatus("OPEN").stream()
                .filter(debt -> debt.customerId.equals(customerId))
                .toList();

        BigDecimal totalDebtCreated = debts.stream().map(debt -> debt.amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCollected = sales.stream().map(sale -> sale.totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CustomerDayHistoryView(
                customer.id,
                customer.name,
                sales.stream()
                        .flatMap(sale -> sale.lines.stream())
                        .map(this::toSaleActivity)
                        .toList(),
                sessions.stream().flatMap(session -> sessionEvents(session).stream()).toList(),
                totalCollected,
                totalDebtCreated
        );
    }

    private SaleActivityView toSaleActivity(SaleLineJpaEntity line) {
        return new SaleActivityView(line.label, line.quantity, line.totalPrice);
    }

    private List<String> sessionEvents(CafeSessionJpaEntity session) {
        var events = new java.util.ArrayList<String>();
        events.add("Session demarree le " + format(session.startedAt));
        if (session.pausedAt != null && session.endedAt == null) {
            events.add("Session en pause depuis le " + format(session.pausedAt));
        }
        if (session.endedAt != null) {
            events.add("Session arretee le " + format(session.endedAt));
        }
        return events;
    }

    private int resolveConsumedMinutes(CafeSessionJpaEntity session) {
        int result;

        if (session.consumedMinutes != null && session.consumedMinutes > 0) {
            result = session.consumedMinutes;
        } else if (session.consumedSeconds != null && session.consumedSeconds > 0) {
            result = Math.max(1, (int) Math.ceil(session.consumedSeconds / 60.0));
        } else if (session.startedAt == null) {
            result = 0;
        } else {
            LocalDateTime end = session.endedAt == null ? LocalDateTime.now() : session.endedAt;
            long durationMinutes = java.time.Duration.between(session.startedAt, end).toMinutes();
            int pausedMinutes = session.pausedMinutes == null ? 0 : session.pausedMinutes;
            result = Math.max(0, (int) durationMinutes - pausedMinutes);
        }

        return result;
    }

    private String resolveSessionState(List<CafeSessionJpaEntity> sessions) {
        boolean hasPausedSession = sessions.stream().anyMatch(session -> session.endedAt == null && session.pausedAt != null);
        String result;

        if (hasPausedSession) {
            result = "Pause";
        } else {
            boolean hasOpenSession = sessions.stream().anyMatch(session -> session.endedAt == null);
            if (hasOpenSession) {
                result = "En cours";
            } else {
                result = sessions.stream()
                        .max(Comparator.comparing(session -> session.startedAt))
                        .map(session -> Boolean.TRUE.equals(session.paid) ? "Paye" : "Terminee")
                        .orElse("Terminee");
            }
        }

        return result;
    }

    private String format(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME_FORMATTER);
    }

}


