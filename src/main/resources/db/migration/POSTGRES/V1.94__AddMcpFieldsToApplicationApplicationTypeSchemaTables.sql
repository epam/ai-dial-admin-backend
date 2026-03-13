-- Add new columns to application_entity table
alter table if exists application_entity add column if not exists mcp_endpoint text;
alter table if exists application_entity add column if not exists mcp_transport text;
alter table if exists application_entity add column if not exists mcp_allowed_tools text;

-- Add new columns to application_entity_aud table
alter table if exists application_entity_aud add column if not exists mcp_endpoint text;
alter table if exists application_entity_aud add column if not exists mcp_transport text;
alter table if exists application_entity_aud add column if not exists mcp_allowed_tools text;

-- Add new columns to application_type_schema_entity table
alter table if exists application_type_schema_entity add column if not exists endpoint text;
alter table if exists application_type_schema_entity add column if not exists transport text;
alter table if exists application_type_schema_entity add column if not exists allowed_tools text;
alter table if exists application_type_schema_entity add column if not exists config_delivery text;
alter table if exists application_type_schema_entity add column if not exists forward_per_request_key boolean;

-- Add new columns to application_type_schema_entity_aud table
alter table if exists application_type_schema_entity_aud add column if not exists endpoint text;
alter table if exists application_type_schema_entity_aud add column if not exists transport text;
alter table if exists application_type_schema_entity_aud add column if not exists allowed_tools text;
alter table if exists application_type_schema_entity_aud add column if not exists config_delivery text;
alter table if exists application_type_schema_entity_aud add column if not exists forward_per_request_key boolean;

--Drop constraint
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

--Add new constraint
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