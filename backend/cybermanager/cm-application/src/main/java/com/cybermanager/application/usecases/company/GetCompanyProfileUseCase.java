package com.cybermanager.application.usecases.company;

import com.cybermanager.application.views.company.CompanyProfileView;
import com.cybermanager.domain.businessobjects.company.CompanyProfile;
import com.cybermanager.domain.ports.company.CompanyProfileRepository;
import com.cybermanager.domain.queries.company.GetCompanyProfileQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCompanyProfileUseCase {

    private final CompanyProfileRepository companyProfileRepository;

    public GetCompanyProfileUseCase(CompanyProfileRepository companyProfileRepository) {
        this.companyProfileRepository = companyProfileRepository;
    }

    @Transactional(readOnly = true)
    public CompanyProfileView execute(GetCompanyProfileQuery query) {
        return companyProfileRepository.findCurrent()
                .map(this::toView)
                .orElse(null);
    }

    private CompanyProfileView toView(CompanyProfile companyProfile) {
        return new CompanyProfileView(
                companyProfile.id(),
                companyProfile.legalName(),
                companyProfile.siret(),
                companyProfile.phone(),
                companyProfile.email(),
                companyProfile.addressLine1(),
                companyProfile.addressLine2(),
                companyProfile.postalCode(),
                companyProfile.city(),
                companyProfile.country(),
                companyProfile.updatedAt()
        );
    }

}
