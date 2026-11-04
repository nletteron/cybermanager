package com.cybermanager.application.usecases.company;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.application.views.company.CompanyProfileView;
import com.cybermanager.domain.businessobjects.company.CompanyProfile;
import com.cybermanager.domain.commands.company.CreateCompanyProfileCommand;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.domain.ports.company.CompanyProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateCompanyProfileUseCase {

    private final CompanyProfileRepository companyProfileRepository;

    public CreateCompanyProfileUseCase(CompanyProfileRepository companyProfileRepository) {
        this.companyProfileRepository = companyProfileRepository;
    }

    @Transactional
    public CompanyProfileView execute(CreateCompanyProfileCommand command) {
        if (companyProfileRepository.findCurrent().isPresent()) {
            throw new BusinessException(BusinessErrorType.CONFLICT, BusinessErrorCode.COMPANY_PROFILE_ALREADY_EXISTS, "Le profil entreprise existe deja.");
        }

        String legalName = normalizeRequired(command.legalName(), BusinessErrorCode.COMPANY_LEGAL_NAME_REQUIRED, "Le nom de l entreprise est requis.");
        LocalDateTime updatedAt = LocalDateTime.now();
        CompanyProfile saved = companyProfileRepository.save(CompanyProfile.create(
                legalName,
                normalize(command.siret()),
                normalize(command.phone()),
                normalize(command.email()),
                normalize(command.addressLine1()),
                normalize(command.addressLine2()),
                normalize(command.postalCode()),
                normalize(command.city()),
                normalize(command.country()),
                updatedAt
        ));

        return toView(saved);
    }

    private String normalizeRequired(String value, BusinessErrorCode code, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new BusinessException(BusinessErrorType.VALIDATION, code, message);
        }
        return normalized;
    }

    private String normalize(String value) {
        String result;

        if (value == null) {
            result = null;
        } else {
            String normalized = value.trim();
            result = normalized.isBlank() ? null : normalized;
        }

        return result;
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
