--  Adapter tables. Fill the display_name with values from the old 'name' column
update adapter_entity set display_name = name where display_name is null or trim(display_name) = '';
update adapter_entity_aud set display_name = name where (display_name is null or trim(display_name) = '') and revtype != 2;
--  Adapter tables. Set the column as not null after all nulls are filled
alter table adapter_entity alter column display_name set not null;

--  Addon tables. Fill the display_name with values from the old 'deployment_name' column
update addon_entity set display_name = deployment_name where display_name is null or trim(display_name) = '';
update addon_entity_aud set display_name = deployment_name where (display_name is null or trim(display_name) = '') and revtype != 2;
--  Addon tables. Set the column as not null after all nulls are filled
alter table addon_entity alter column display_name set not null;

--  Application tables. Fill the display_name with values from the old 'deployment_name' column
update application_entity set display_name = deployment_name where display_name is null or trim(display_name) = '';
update application_entity_aud set display_name = deployment_name where (display_name is null or trim(display_name) = '') and revtype != 2;
--  Application tables. Set the column as not null after all nulls are filled
alter table application_entity alter column display_name set not null;

--  Application type schema tables. Fill the display_name with values from the old 'schema_id' column
update application_type_schema_entity set application_type_display_name = schema_id where (display_name is null or trim(display_name) = '') and revtype != 2;
update application_type_schema_entity_aud set application_type_display_name = schema_id where (application_type_display_name is null or trim(application_type_display_name) = '') and revtype != 2;
--  Application type schema tables. Set the column as not null after all nulls are filled
alter table application_type_schema_entity alter column application_type_display_name set not null;

--  Assistant tables. Fill the display_name with values from the old 'deployment_name' column
update assistant_entity set display_name = deployment_name where display_name is null or trim(display_name) = '';
update assistant_entity_aud set display_name = deployment_name where (display_name is null or trim(display_name) = '') and revtype != 2;
--  Assistant tables. Set the column as not null after all nulls are filled
alter table assistant_entity alter column display_name set not null;

--  Interceptor tables. Fill the display_name with values from the old 'name' column
update interceptor_entity set display_name = name where display_name is null or trim(display_name) = '';
update interceptor_entity_aud set display_name = name where (display_name is null or trim(display_name) = '') and revtype != 2;
--  Interceptor tables. Set the column as not null after all nulls are filled
alter table interceptor_entity alter column display_name set not null;

--  Interceptor Runner tables. Fill the display_name with values from the old 'name' column
update interceptor_runner_entity set display_name = name where display_name is null or trim(display_name) = '';
update interceptor_runner_entity_aud set display_name = name where (display_name is null or trim(display_name) = '') and revtype != 2;
--  Interceptor Runner tables. Set the column as not null after all nulls are filled
alter table interceptor_runner_entity alter column display_name set not null;

--  Key tables. Fill the display_name with values from the old 'name' column
update key_entity set display_name = name where display_name is null or trim(display_name) = '';
update key_entity_aud set display_name = name where (display_name is null or trim(display_name) = '') and revtype != 2;
--  Key Runner tables. Set the column as not null after all nulls are filled
alter table key_entity alter column display_name set not null;

--  Model tables. Fill the display_name with values from the old 'deployment_name' column
update model_entity set display_name = deployment_name where display_name is null or trim(display_name) = '';
update model_entity_aud set display_name = deployment_name where (display_name is null or trim(display_name) = '') and revtype != 2;
--  Model tables. Set the column as not null after all nulls are filled
alter table model_entity alter column display_name set not null;

--  Role tables. Fill the display_name with values from the old 'name' column
update role_entity set display_name = name where display_name is null or trim(display_name) = '';
update role_entity_aud set display_name = name where (display_name is null or trim(display_name) = '') and revtype != 2;
--  Role tables. Set the column as not null after all nulls are filled
alter table role_entity alter column display_name set not null;

--  Route tables. Fill the display_name with values from the old 'deployment_name' column
update route_entity set display_name = deployment_name where display_name is null or trim(display_name) = '';
update route_entity_aud set display_name = deployment_name where (display_name is null or trim(display_name) = '') and revtype != 2;
--  Route tables. Set the column as not null after all nulls are filled
alter table route_entity alter column display_name set not null;

--  Toolset tables. Fill the display_name with values from the old 'deployment_name' column
update tool_set_entity set display_name = deployment_name where display_name is null or trim(display_name) = '';
update tool_set_entity_aud set display_name = deployment_name where (display_name is null or trim(display_name) = '') and revtype != 2;
--  Toolset tables. Set the column as not null after all nulls are filled
alter table tool_set_entity alter column display_name set not null;