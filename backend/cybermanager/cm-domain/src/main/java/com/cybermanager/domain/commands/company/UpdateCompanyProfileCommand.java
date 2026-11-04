package com.cybermanager.domain.commands.company;

public record UpdateCompanyProfileCommand(
        String actorEmail,
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
