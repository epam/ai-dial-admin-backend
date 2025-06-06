update application_entity set endpoint = '' where application_type_schema_id is not null;

alter table application_entity add constraint CHK_APPLICATION_ENTITY_ENDPOINT_APPLICATION_TYPE_SCHEMA_ID
check (
    (nullif(endpoint,'') is null and nullif(application_type_schema_id,'') is not null)
    or
    (nullif(endpoint,'') is not null and nullif(application_type_schema_id,'') is null)
);