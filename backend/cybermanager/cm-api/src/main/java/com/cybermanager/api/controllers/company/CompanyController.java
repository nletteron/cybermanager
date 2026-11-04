package com.cybermanager.api.controllers.company;

import com.cybermanager.api.abstracts.ApiSupport;
import com.cybermanager.api.dtos.company.CompanyProfileRequest;
import com.cybermanager.api.dtos.company.CompanyProfileResponse;
import com.cybermanager.api.mappers.company.CompanyApiMapper;
import com.cybermanager.application.usecases.company.CreateCompanyProfileUseCase;
import com.cybermanager.application.usecases.company.GetCompanyProfileUseCase;
import com.cybermanager.application.usecases.company.UpdateCompanyProfileUseCase;
import com.cybermanager.application.views.company.CompanyProfileView;
import com.cybermanager.domain.queries.company.GetCompanyProfileQuery;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final GetCompanyProfileUseCase getCompanyProfileUseCase;
    private final CreateCompanyProfileUseCase createCompanyProfileUseCase;
    private final UpdateCompanyProfileUseCase updateCompanyProfileUseCase;
    private final JwtAccessTokenReader tokenReader;
    private final CompanyApiMapper companyApiMapper;

    public CompanyController(
            GetCompanyProfileUseCase getCompanyProfileUseCase,
            CreateCompanyProfileUseCase createCompanyProfileUseCase,
            UpdateCompanyProfileUseCase updateCompanyProfileUseCase,
            JwtAccessTokenReader tokenReader,
            CompanyApiMapper companyApiMapper
    ) {
        this.getCompanyProfileUseCase = getCompanyProfileUseCase;
        this.createCompanyProfileUseCase = createCompanyProfileUseCase;
        this.updateCompanyProfileUseCase = updateCompanyProfileUseCase;
        this.tokenReader = tokenReader;
        this.companyApiMapper = companyApiMapper;
    }

    @GetMapping
    public ResponseEntity<CompanyProfileResponse> current() {
        CompanyProfileView view = getCompanyProfileUseCase.execute(new GetCompanyProfileQuery());
        return ResponseEntity.ok(view == null ? null : toResponse(view));
    }

    @PostMapping
    public ResponseEntity<CompanyProfileResponse> create(@RequestHeader("Authorization") String authorization, @RequestBody CompanyProfileRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = createCompanyProfileUseCase.execute(companyApiMapper.toCreateCompanyProfileCommand(actor.email(), request));
        return ResponseEntity.ok(toResponse(view));
    }

    @PutMapping
    public ResponseEntity<CompanyProfileResponse> update(@RequestHeader("Authorization") String authorization, @RequestBody CompanyProfileRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = updateCompanyProfileUseCase.execute(companyApiMapper.toUpdateCompanyProfileCommand(actor.email(), request));
        return ResponseEntity.ok(toResponse(view));
    }

    private CompanyProfileResponse toResponse(CompanyProfileView view) {
        return new CompanyProfileResponse(
                view.id(),
                view.legalName(),
                view.siret(),
                view.phone(),
                view.email(),
                view.addressLine1(),
                view.addressLine2(),
                view.postalCode(),
                view.city(),
                view.country(),
                view.updatedAt()
        );
    }

}


