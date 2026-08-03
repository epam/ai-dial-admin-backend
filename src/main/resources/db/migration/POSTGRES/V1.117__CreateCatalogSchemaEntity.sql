CREATE TABLE catalog_schema_entity (
    schema_id varchar(850) not null PRIMARY KEY,
    schema text,
    type varchar(64),
    title text,
    description text,
    catalog_entity_type varchar(50),
    catalog_display_name text,
    default_locale varchar(10),
    defs text,
    properties text,
    required text array,
    topics text array,
    created_at_ms bigint not null,
    updated_at_ms bigint not null
);

CREATE TABLE catalog_schema_entity_aud (
    schema_id varchar(850) not null,
    rev integer not null,
    revtype smallint,
    schema text,
    type varchar(64),
    title text,
    description text,
    catalog_entity_type varchar(50),
    catalog_display_name text,
    default_locale varchar(10),
    defs text,
    properties text,
    required text array,
    topics text array,
    created_at_ms bigint,
    updated_at_ms bigint,
    PRIMARY KEY (rev, schema_id)
);

alter table if exists catalog_schema_entity_aud add constraint FK_REVINFO_CATALOG_SCHEMA_ENTITY_AUD foreign key (rev) references revinfo;

ALTER TABLE application_entity ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE application_entity ADD COLUMN catalog_properties text;
ALTER TABLE application_entity ADD CONSTRAINT FK_APPLICATION_CATALOG_SCHEMA FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE model_entity ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE model_entity ADD COLUMN catalog_properties text;
ALTER TABLE model_entity ADD CONSTRAINT FK_MODEL_CATALOG_SCHEMA FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE tool_set_entity ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE tool_set_entity ADD COLUMN catalog_properties text;
ALTER TABLE tool_set_entity ADD CONSTRAINT FK_TOOLSET_CATALOG_SCHEMA FOREIGN KEY (catalog_schema_id) REFERENCES catalog_schema_entity(schema_id);

ALTER TABLE application_entity_aud ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE application_entity_aud ADD COLUMN catalog_properties text;

ALTER TABLE model_entity_aud ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE model_entity_aud ADD COLUMN catalog_properties text;

ALTER TABLE tool_set_entity_aud ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE tool_set_entity_aud ADD COLUMN catalog_properties text;
