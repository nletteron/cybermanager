package com.cybermanager.api.mappers.company;

import com.cybermanager.api.dtos.company.CompanyProfileRequest;
import com.cybermanager.domain.commands.company.CreateCompanyProfileCommand;
import com.cybermanager.domain.commands.company.UpdateCompanyProfileCommand;
import org.springframework.stereotype.Component;

@Component
public class CompanyApiMapper {

    public CreateCompanyProfileCommand toCreateCompanyProfileCommand(String actorEmail, CompanyProfileRequest request) {
        return new CreateCompanyProfileCommand(
                actorEmail,
                request.legalName(),
                request.siret(),
                request.phone(),
                request.email(),
                request.addressLine1(),
                request.addressLine2(),
                request.postalCode(),
                request.city(),
                request.country()
        );
    }

    public UpdateCompanyProfileCommand toUpdateCompanyProfileCommand(String actorEmail, CompanyProfileRequest request) {
        return new UpdateCompanyProfileCommand(
                actorEmail,
                request.legalName(),
                request.siret(),
                request.phone(),
                request.email(),
                request.addressLine1(),
                request.addressLine2(),
                request.postalCode(),
                request.city(),
                request.country()
        );
    }

}
