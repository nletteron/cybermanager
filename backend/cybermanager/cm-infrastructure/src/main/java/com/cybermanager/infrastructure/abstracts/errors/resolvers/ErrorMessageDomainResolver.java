package com.cybermanager.infrastructure.abstracts.errors.resolvers;

import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.DomainException;
import com.cybermanager.infrastructure.abstracts.errors.ErrorDescriptor;

import java.util.Map;

public interface ErrorMessageDomainResolver {

    ErrorDescriptor resolve(DomainException exception);

    ErrorDescriptor resolve(BusinessErrorCode domainCode, Map<String, Object> parameters);

}
