package com.cybermanager.api.dtos.company;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyProfileResponse(
        UUID id,
        String legalName,
        String siret,
        String phone,
        String email,
        String addressLine1,
        String addressLine2,
        String postalCode,
        String city,
        String country,
        LocalDateTime updatedAt
) {

}
