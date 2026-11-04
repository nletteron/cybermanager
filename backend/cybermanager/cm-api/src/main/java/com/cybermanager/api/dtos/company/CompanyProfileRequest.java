package com.cybermanager.api.dtos.company;

public record CompanyProfileRequest(
        String legalName,
        String siret,
        String phone,
        String email,
        String addressLine1,
        String addressLine2,
        String postalCode,
        String city,
        String country
) {

}
