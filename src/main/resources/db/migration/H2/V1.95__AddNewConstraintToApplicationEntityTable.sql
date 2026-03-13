alter table application_entity
drop constraint if exists CHK_APPLICATION_ENTITY_ENDPOINT_APPLICATION_TYPE_SCHEMA_ID;

update application_entity set endpoint = '' where application_type_schema_id is not null;
update application_entity set mcp_endpoint = '' where application_type_schema_id is not null;

alter table application_entity add constraint CHK_APPLICATION_ENTITY_ENDPOINT_APPLICATION_TYPE_SCHEMA_ID_MCP_ENDPOINT
check (
    (
        nullif(application_type_schema_id,'') is not null
        and nullif(endpoint,'') is null
        and nullif(mcp_endpoint,'') is null
    )
    or
    (
        nullif(application_type_schema_id,'') is null
        and (
            nullif(endpoint,'') is not null
            or nullif(mcp_endpoint,'') is not null
        )
    )
);