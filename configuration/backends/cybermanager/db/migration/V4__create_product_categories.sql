CREATE TABLE cybermanager.cm_product_categories (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    vat_rate NUMERIC(5, 2) NOT NULL,
    CONSTRAINT pk_cm_product_categories PRIMARY KEY (id),
    CONSTRAINT uk_cm_product_categories_name UNIQUE (name),
    CONSTRAINT ck_cm_product_categories_vat_rate CHECK (vat_rate >= 0 AND vat_rate <= 100)
);

ALTER TABLE cybermanager.cm_products ADD COLUMN category_id UUID;
ALTER TABLE cybermanager.cm_products ADD CONSTRAINT fk_cm_products_category
    FOREIGN KEY (category_id) REFERENCES cybermanager.cm_product_categories (id);
CREATE INDEX idx_cm_products_category_id ON cybermanager.cm_products (category_id);
