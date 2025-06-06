-- drop 'id' column
alter table assistant_entity drop column (id);
-- add primary key which refers to 'deployment_name' column
alter table assistant_entity add primary key (deployment_name);
-- drop sequence since it's no longer used for ids generation. 'deployment_name' column becomes id and it comes from outside
drop sequence assistant_entity_seq;