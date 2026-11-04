package com.cybermanager.infrastructure.out.entities.errors;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cm_error_catalog")
public class ErrorCatalogJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String domainCode;

    @Column(nullable = false, unique = true)
    private String errorCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ErrorCatalogTypeJpa type;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private int httpStatus;

    @Column(nullable = false)
    private String boundedContext;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDomainCode() {
        return domainCode;
    }

    public void setDomainCode(String domainCode) {
        this.domainCode = domainCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCatalogTypeJpa getType() {
        return type;
    }

    public void setType(ErrorCatalogTypeJpa type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getBoundedContext() {
        return boundedContext;
    }

    public void setBoundedContext(String boundedContext) {
        this.boundedContext = boundedContext;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
