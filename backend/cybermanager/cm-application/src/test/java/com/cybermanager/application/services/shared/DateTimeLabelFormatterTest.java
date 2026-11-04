package com.cybermanager.application.services.shared;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateTimeLabelFormatterTest {

    @Test
    void shouldFormatDateTimeForBusinessExports() {
        String result = DateTimeLabelFormatter.format(LocalDateTime.of(2026, 8, 17, 18, 45));

        assertEquals("17/08/2026 18:45", result);
    }

}
