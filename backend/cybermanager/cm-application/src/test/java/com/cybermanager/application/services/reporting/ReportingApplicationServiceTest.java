package com.cybermanager.application.services.reporting;

import com.cybermanager.application.ports.reporting.DayHistoryQuery;
import com.cybermanager.application.views.reporting.CustomerDayHistoryView;
import com.cybermanager.application.views.reporting.DayHistoryView;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportingApplicationServiceTest {

    @Test
    void shouldDelegateDayHistoryLookup() {
        DayHistoryQuery queryAdapter = mock(DayHistoryQuery.class);
        ReportingApplicationService service = new ReportingApplicationService(queryAdapter);
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 17);
        DayHistoryView history = mock(DayHistoryView.class);

        when(queryAdapter.getDayHistory(startDate, endDate)).thenReturn(history);

        assertSame(history, service.dayHistory(startDate, endDate));
    }

    @Test
    void shouldDelegateCustomerHistoryLookup() {
        DayHistoryQuery queryAdapter = mock(DayHistoryQuery.class);
        ReportingApplicationService service = new ReportingApplicationService(queryAdapter);
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 17);
        UUID customerId = UUID.randomUUID();
        CustomerDayHistoryView history = mock(CustomerDayHistoryView.class);

        when(queryAdapter.getCustomerDayHistory(startDate, endDate, customerId)).thenReturn(history);

        assertSame(history, service.customerHistory(startDate, endDate, customerId));
    }

}
