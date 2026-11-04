package com.cybermanager.domain.ports.company;

import com.cybermanager.domain.businessobjects.company.CompanyProfile;

import java.util.Optional;

public interface CompanyProfileRepository {

    Optional<CompanyProfile> findCurrent();

    CompanyProfile save(CompanyProfile companyProfile);

}
