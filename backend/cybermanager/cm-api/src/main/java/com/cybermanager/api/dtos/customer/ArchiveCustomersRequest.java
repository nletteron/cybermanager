package com.cybermanager.api.dtos.customer;

import java.time.LocalDate;

public record ArchiveCustomersRequest(LocalDate startDate, LocalDate endDate, String type, String format) {

}
