-- Add new columns to application_entity table
alter table application_entity add mcp_endpoint nvarchar(max);
alter table application_entity add mcp_transport nvarchar(max);
alter table application_entity add mcp_allowed_tools nvarchar(max);

-- Add new columns to application_entity_aud table
alter table application_entity_aud add mcp_endpoint nvarchar(max);
alter table application_entity_aud add mcp_transport nvarchar(max);
alter table application_entity_aud add mcp_allowed_tools nvarchar(max);

-- Add new columns to application_type_schema_entity table
alter table application_type_schema_entity add endpoint nvarchar(max);
alter table application_type_schema_entity add transport nvarchar(max);
alter table application_type_schema_entity add allowed_tools nvarchar(max);
alter table application_type_schema_entity add config_delivery nvarchar(max);
alter table application_type_schema_entity add forward_per_request_key bit;

-- Add new columns to application_type_schema_entity_aud table
alter table application_type_schema_entity_aud add endpoint nvarchar(max);
alter table application_type_schema_entity_aud add transport nvarchar(max);
alter table application_type_schema_entity_aud add allowed_tools nvarchar(max);
alter table application_type_schema_entity_aud add config_delivery nvarchar(max);
alter table application_type_schema_entity_aud add forward_per_request_key bit;

go
--Drop constraint
declare @constraint_name nvarchar(200);

select @constraint_name = cc.name
from sys.check_constraints cc
join sys.objects o on cc.parent_object_id = o.object_id
where o.name = 'application_entity'
and cc.definition like '%endpoint%'
and cc.definition like '%application_type_schema_id%';

if @constraint_name is not null
begin
    exec('alter table application_entity drop constraint ' + @constraint_name);
end

-- Add new constraint
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