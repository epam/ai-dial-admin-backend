-- add new field display_name to route_entity, route_entity_aud tables
alter table route_entity add display_name nvarchar(max);
alter table route_entity_aud add display_name nvarchar(max);
