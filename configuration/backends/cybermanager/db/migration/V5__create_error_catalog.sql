CREATE TABLE IF NOT EXISTS cybermanager.cm_error_catalog (
    id UUID NOT NULL,
    domain_code VARCHAR(255) NOT NULL,
    error_code VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    http_status INTEGER NOT NULL,
    bounded_context VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_cm_error_catalog PRIMARY KEY (id),
    CONSTRAINT uk_cm_error_catalog_domain_code UNIQUE (domain_code),
    CONSTRAINT uk_cm_error_catalog_error_code UNIQUE (error_code)
);

CREATE INDEX IF NOT EXISTS idx_cm_error_catalog_active_type
    ON cybermanager.cm_error_catalog (active, type);
