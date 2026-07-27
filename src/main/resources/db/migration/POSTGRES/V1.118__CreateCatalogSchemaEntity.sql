CREATE TABLE catalog_schema_entity (
    schema_id VARCHAR(512) PRIMARY KEY,
    schema TEXT,
    type VARCHAR(50),
    title VARCHAR(512),
    description VARCHAR(2048),
    catalog_entity_type VARCHAR(50),
    catalog_display_name VARCHAR(512),
    default_locale VARCHAR(10),
    defs TEXT,
    properties TEXT,
    required TEXT ARRAY,
    topics TEXT ARRAY,
    created_at_ms BIGINT NOT NULL,
    updated_at_ms BIGINT NOT NULL
);

CREATE TABLE catalog_schema_entity_aud (
    schema_id VARCHAR(512) NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    schema TEXT,
    type VARCHAR(50),
    title VARCHAR(512),
    description VARCHAR(2048),
    catalog_entity_type VARCHAR(50),
    catalog_display_name VARCHAR(512),
    default_locale VARCHAR(10),
    defs TEXT,
    properties TEXT,
    required TEXT ARRAY,
    topics TEXT ARRAY,
    created_at_ms BIGINT,
    updated_at_ms BIGINT,
    PRIMARY KEY (schema_id, rev)
);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'revinfo') THEN
        ALTER TABLE catalog_schema_entity_aud ADD CONSTRAINT FK_REVINFO_CATALOG_SCHEMA_ENTITY_AUD FOREIGN KEY (rev) REFERENCES revinfo(rev);
    END IF;
END $$;

ALTER TABLE application_entity ADD COLUMN catalog_schema_id VARCHAR(512);
ALTER TABLE application_entity ADD COLUMN catalog_properties TEXT;
ALTER TABLE application_entity ADD CONSTRAINT fk_application_catalog_schema FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE model_entity ADD COLUMN catalog_schema_id VARCHAR(512);
ALTER TABLE model_entity ADD COLUMN catalog_properties TEXT;
ALTER TABLE model_entity ADD CONSTRAINT fk_model_catalog_schema FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE tool_set_entity ADD COLUMN catalog_schema_id VARCHAR(512);
ALTER TABLE tool_set_entity ADD COLUMN catalog_properties TEXT;
ALTER TABLE tool_set_entity ADD CONSTRAINT fk_toolset_catalog_schema FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE application_entity_aud ADD COLUMN catalog_schema_id VARCHAR(512);
ALTER TABLE application_entity_aud ADD COLUMN catalog_properties TEXT;

ALTER TABLE model_entity_aud ADD COLUMN catalog_schema_id VARCHAR(512);
ALTER TABLE model_entity_aud ADD COLUMN catalog_properties TEXT;

ALTER TABLE tool_set_entity_aud ADD COLUMN catalog_schema_id VARCHAR(512);
ALTER TABLE tool_set_entity_aud ADD COLUMN catalog_properties TEXT;
