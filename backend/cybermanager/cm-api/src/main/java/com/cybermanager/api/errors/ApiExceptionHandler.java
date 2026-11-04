package com.cybermanager.api.errors;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.application.usecases.users.UserManagementException;
import com.cybermanager.domain.exceptions.DomainException;
import com.cybermanager.infrastructure.abstracts.errors.ErrorDescriptor;
import com.cybermanager.infrastructure.abstracts.errors.InfrastructureErrorCode;
import com.cybermanager.infrastructure.abstracts.errors.resolvers.ErrorMessageDomainResolver;
import com.cybermanager.infrastructure.abstracts.errors.resolvers.ErrorMessageInfrastructureResolver;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final ErrorMessageDomainResolver domainResolver;
    private final ErrorMessageInfrastructureResolver infrastructureResolver;

    public ApiExceptionHandler(
            ErrorMessageDomainResolver domainResolver,
            ErrorMessageInfrastructureResolver infrastructureResolver
    ) {
        this.domainResolver = domainResolver;
        this.infrastructureResolver = infrastructureResolver;
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiErrorResponse> handleDomain(DomainException exception) {
        ErrorDescriptor descriptor = domainResolver.resolve(exception);
        LOGGER.warn(
                "Domain error handled domainCode={} code={} status={} message={}",
                descriptor.domainCode(),
                descriptor.code(),
                descriptor.httpStatus(),
                descriptor.message()
        );
        return toResponse(descriptor);
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException exception) {
        return handleDomain(exception);
    }

    @ExceptionHandler(JwtException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidToken(JwtException exception) {
        LOGGER.debug("API token rejected message={}", exception.getMessage());
        return toResponse(infrastructureResolver.resolve(InfrastructureErrorCode.INVALID_TOKEN));
    }

    @ExceptionHandler(UserManagementException.class)
    ResponseEntity<ApiErrorResponse> handleUserBusiness(UserManagementException exception) {
        return handleBusiness((BusinessException) exception);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception) {
        LOGGER.warn("Bad request handled message={}", exception.getMessage());
        return toResponse(infrastructureResolver.resolve(InfrastructureErrorCode.BAD_REQUEST));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(NoResourceFoundException exception) {
        LOGGER.debug("API route not found path={}", exception.getResourcePath());
        return toResponse(infrastructureResolver.resolve(InfrastructureErrorCode.ROUTE_NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        LOGGER.debug(
                "API method not allowed method={} supportedMethods={}",
                exception.getMethod(),
                exception.getSupportedHttpMethods()
        );
        return toResponse(infrastructureResolver.resolve(InfrastructureErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnhandled(Exception exception) {
        LOGGER.error("Unhandled server error", exception);
        return toResponse(infrastructureResolver.resolve(InfrastructureErrorCode.INTERNAL_ERROR));
    }

    private ResponseEntity<ApiErrorResponse> toResponse(ErrorDescriptor descriptor) {
        return ResponseEntity.status(HttpStatus.valueOf(descriptor.httpStatus()))
                .body(new ApiErrorResponse(descriptor.code(), descriptor.message(), descriptor.httpStatus(), descriptor.timestamp()));
    }

}

