-- drop 'id' column
alter table application_type_schema_entity drop column (id);
-- make 'schema_id' column not nullable
alter table application_type_schema_entity alter column schema_id varchar(255) not null;
-- add primary key which refers to 'schema_id' column
alter table application_type_schema_entity add primary key (schema_id);

-- drop foreign key constraint which refers to 'schema_id' column
alter table application_entity drop constraint FKBK70TYY2OP3LS9CQ6KFRTGL36;
-- drop unique constraint which refers to 'schema_id' column
alter table application_type_schema_entity drop constraint UK3R0C7HHVQ8T10XBCO2M0F82LH;
-- add foreign key to 'application_type_schema_id' column which references 'schema_id' column from application_type_schema_entity table
alter table application_entity add constraint FK_APPLICATION_TYPE_SCHEMA_ENTITY_APPLICATION_TYPE_SCHEMA_ID foreign key (application_type_schema_id) references application_type_schema_entity (schema_id);

-- drop sequence since it's no longer used for ids generation. 'schema_id' column becomes id and it comes from outside
drop sequence application_type_schema_entity_seq;