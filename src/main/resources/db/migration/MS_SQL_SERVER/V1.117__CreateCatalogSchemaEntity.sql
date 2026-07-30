CREATE TABLE catalog_schema_entity (
    schema_id nvarchar(850) not null PRIMARY KEY,
    [schema] nvarchar(max),
    type varchar(64),
    title nvarchar(max),
    description nvarchar(max),
    catalog_entity_type nvarchar(50),
    catalog_display_name nvarchar(512),
    default_locale nvarchar(10),
    defs nvarchar(max),
    properties nvarchar(max),
    required varbinary(max),
    topics varbinary(max),
    created_at_ms bigint not null,
    updated_at_ms bigint not null
);

CREATE TABLE catalog_schema_entity_aud (
    schema_id nvarchar(850) not null,
    rev integer not null,
    revtype tinyint,
    [schema] nvarchar(max),
    type varchar(64),
    title nvarchar(max),
    description nvarchar(max),
    catalog_entity_type nvarchar(50),
    catalog_display_name nvarchar(512),
    default_locale nvarchar(10),
    defs nvarchar(max),
    properties nvarchar(max),
    required varbinary(max),
    topics varbinary(max),
    created_at_ms bigint,
    updated_at_ms bigint,
    PRIMARY KEY (rev, schema_id)
);

alter table catalog_schema_entity_aud add constraint FK_REVINFO_CATALOG_SCHEMA_ENTITY_AUD foreign key (rev) references revinfo;

ALTER TABLE application_entity ADD catalog_schema_id nvarchar(850);
ALTER TABLE application_entity ADD catalog_properties nvarchar(max);
ALTER TABLE application_entity ADD CONSTRAINT fk_application_catalog_schema FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE model_entity ADD catalog_schema_id nvarchar(850);
ALTER TABLE model_entity ADD catalog_properties nvarchar(max);
ALTER TABLE model_entity ADD CONSTRAINT fk_model_catalog_schema FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE tool_set_entity ADD catalog_schema_id nvarchar(850);
ALTER TABLE tool_set_entity ADD catalog_properties nvarchar(max);
ALTER TABLE tool_set_entity ADD CONSTRAINT fk_toolset_catalog_schema FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE application_entity_aud ADD catalog_schema_id nvarchar(850);
ALTER TABLE application_entity_aud ADD catalog_properties nvarchar(max);

ALTER TABLE model_entity_aud ADD catalog_schema_id nvarchar(850);
ALTER TABLE model_entity_aud ADD catalog_properties nvarchar(max);

ALTER TABLE tool_set_entity_aud ADD catalog_schema_id nvarchar(850);
ALTER TABLE tool_set_entity_aud ADD catalog_properties nvarchar(max);
