package com.cybermanager.infrastructure.abstracts.errors.resolvers;

import com.cybermanager.infrastructure.abstracts.errors.ErrorDescriptor;
import com.cybermanager.infrastructure.abstracts.errors.InfrastructureErrorCode;
import com.cybermanager.infrastructure.abstracts.errors.InfrastructureException;

import java.util.Map;

public interface ErrorMessageInfrastructureResolver {

    ErrorDescriptor resolve(InfrastructureErrorCode code);

    ErrorDescriptor resolve(InfrastructureException exception);

    ErrorDescriptor resolve(InfrastructureErrorCode code, Map<String, Object> parameters);

}
