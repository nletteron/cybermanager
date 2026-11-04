package com.cybermanager.application.services.monitoring;

import com.cybermanager.application.ports.monitoring.DailyMonitoringQuery;
import com.cybermanager.application.views.monitoring.DailyCustomerActivityView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonitoringApplicationServiceTest {

    @Test
    void shouldDelegateDailyCustomersLookup() {
        DailyMonitoringQuery queryAdapter = mock(DailyMonitoringQuery.class);
        MonitoringApplicationService service = new MonitoringApplicationService(queryAdapter);

        when(queryAdapter.getDailyCustomers()).thenReturn(List.of());

        assertEquals(List.of(), service.customers());
    }

    @Test
    void shouldDelegateCustomerActivityLookup() {
        DailyMonitoringQuery queryAdapter = mock(DailyMonitoringQuery.class);
        MonitoringApplicationService service = new MonitoringApplicationService(queryAdapter);
        UUID customerId = UUID.randomUUID();
        DailyCustomerActivityView activity = mock(DailyCustomerActivityView.class);

        when(queryAdapter.getDailyCustomerActivity(customerId)).thenReturn(activity);

        assertSame(activity, service.customerActivity(customerId));
    }

}
