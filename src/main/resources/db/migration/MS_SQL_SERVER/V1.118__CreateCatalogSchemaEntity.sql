CREATE TABLE catalog_schema_entity (
    schema_id VARCHAR(512) PRIMARY KEY,
    schema VARCHAR(MAX),
    type VARCHAR(50),
    title VARCHAR(512),
    description VARCHAR(2048),
    catalog_entity_type VARCHAR(50),
    catalog_display_name VARCHAR(512),
    default_locale VARCHAR(10),
    defs VARCHAR(MAX),
    properties VARCHAR(MAX),
    required VARBINARY(MAX),
    topics NVARCHAR(MAX),
    created_at_ms BIGINT NOT NULL,
    updated_at_ms BIGINT NOT NULL
);

CREATE TABLE catalog_schema_entity_aud (
    schema_id VARCHAR(512) NOT NULL,
    rev INTEGER NOT NULL,
    revtype TINYINT,
    schema VARCHAR(MAX),
    type VARCHAR(50),
    title VARCHAR(512),
    description VARCHAR(2048),
    catalog_entity_type VARCHAR(50),
    catalog_display_name VARCHAR(512),
    default_locale VARCHAR(10),
    defs VARCHAR(MAX),
    properties VARCHAR(MAX),
    required VARBINARY(MAX),
    topics NVARCHAR(MAX),
    created_at_ms BIGINT,
    updated_at_ms BIGINT,
    PRIMARY KEY (schema_id, rev)
);

IF OBJECT_ID('revinfo', 'U') IS NOT NULL
BEGIN
    ALTER TABLE catalog_schema_entity_aud ADD CONSTRAINT FK_REVINFO_CATALOG_SCHEMA_ENTITY_AUD FOREIGN KEY (rev) REFERENCES revinfo(rev);
END;

ALTER TABLE application_entity ADD catalog_schema_id NVARCHAR(512);
ALTER TABLE application_entity ADD catalog_properties NVARCHAR(MAX);
ALTER TABLE application_entity ADD CONSTRAINT fk_application_catalog_schema FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE model_entity ADD catalog_schema_id NVARCHAR(512);
ALTER TABLE model_entity ADD catalog_properties NVARCHAR(MAX);
ALTER TABLE model_entity ADD CONSTRAINT fk_model_catalog_schema FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE tool_set_entity ADD catalog_schema_id NVARCHAR(512);
ALTER TABLE tool_set_entity ADD catalog_properties NVARCHAR(MAX);
ALTER TABLE tool_set_entity ADD CONSTRAINT fk_toolset_catalog_schema FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE application_entity_aud ADD catalog_schema_id NVARCHAR(512);
ALTER TABLE application_entity_aud ADD catalog_properties NVARCHAR(MAX);

ALTER TABLE model_entity_aud ADD catalog_schema_id NVARCHAR(512);
ALTER TABLE model_entity_aud ADD catalog_properties NVARCHAR(MAX);

ALTER TABLE tool_set_entity_aud ADD catalog_schema_id NVARCHAR(512);
ALTER TABLE tool_set_entity_aud ADD catalog_properties NVARCHAR(MAX);
