--Drop constraint
alter table application_entity
drop constraint if exists CHK_APPLICATION_ENTITY_ENDPOINT_APPLICATION_TYPE_SCHEMA_ID_MCP_ENDPOINT;

-- Add new constraint allowing interfaces as an alternative routing configuration
alter table application_entity add constraint CHK_APPLICATION_ENTITY_ENDPOINT_APPLICATION_TYPE_SCHEMA_ID_MCP_ENDPOINT
check (
    (
        nullif(application_type_schema_id,'') is not null
        and nullif(endpoint,'') is null
        and nullif(mcp_endpoint,'') is null
        and nullif(nullif(interfaces,''),'{}') is null
    )
    or
    (
        nullif(application_type_schema_id,'') is null
        and (
            nullif(endpoint,'') is not null
            or nullif(mcp_endpoint,'') is not null
            or nullif(nullif(interfaces,''),'{}') is not null
        )
    )
);
