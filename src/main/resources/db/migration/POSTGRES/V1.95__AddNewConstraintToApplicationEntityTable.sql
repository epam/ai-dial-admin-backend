do $$
declare constraint_name text;
begin
    select conname
    into constraint_name
    from pg_constraint
    where conrelid = 'application_entity'::regclass
    and contype = 'c'
    and pg_get_constraintdef(oid) like '%endpoint%'
    and pg_get_constraintdef(oid) like '%application_type_schema_id%';

    if constraint_name is not null then
        execute 'alter table application_entity drop constraint ' || constraint_name;
    end if;
end $$;

update application_entity
set endpoint = ''
where application_type_schema_id is not null
and nullif(endpoint,'') is not null;

update application_entity
set mcp_endpoint = ''
where application_type_schema_id is not null
and nullif(mcp_endpoint,'') is not null;

alter table application_entity
add constraint chk_application_entity_endpoint_schema_mcp
check (
    (
        application_type_schema_id is not null
        and nullif(endpoint,'') is null
        and nullif(mcp_endpoint,'') is null
    )
    or
    (
        application_type_schema_id is null
        and (
            nullif(endpoint,'') is not null
            or nullif(mcp_endpoint,'') is not null
        )
    )
);