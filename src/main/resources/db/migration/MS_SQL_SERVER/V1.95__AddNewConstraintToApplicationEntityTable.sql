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