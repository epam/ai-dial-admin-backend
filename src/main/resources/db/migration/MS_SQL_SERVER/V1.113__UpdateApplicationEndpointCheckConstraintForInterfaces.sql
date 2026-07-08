--Drop constraint
alter table application_entity
drop constraint if exists chk_application_entity_endpoint_schema_mcp;

-- Add new constraint allowing interfaces as an alternative routing configuration
alter table application_entity
add constraint chk_application_entity_endpoint_schema_mcp
check (
    (
        application_type_schema_id is not null
        and nullif(endpoint,'') is null
        and nullif(mcp_endpoint,'') is null
        and nullif(nullif(interfaces,''),'{}') is null
    )
    or
    (
        application_type_schema_id is null
        and (
            nullif(endpoint,'') is not null
            or nullif(mcp_endpoint,'') is not null
            or nullif(nullif(interfaces,''),'{}') is not null
        )
    )
);
