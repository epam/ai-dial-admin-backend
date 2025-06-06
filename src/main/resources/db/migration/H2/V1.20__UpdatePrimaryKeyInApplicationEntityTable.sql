-- add 'application_name' column into interceptor_application table
alter table interceptor_application add application_name varchar(255);
-- set 'application_name' column values equal to 'deployment_name' column values from application_entity table joined by 'application_id' column
update interceptor_application ia set ia.application_name = (select ae.deployment_name from application_entity ae where ae.id = ia.application_id);
-- make 'application_name' column not null
alter table interceptor_application alter column application_name varchar(255) not null;
-- drop 'application_id' column
alter table interceptor_application drop column (application_id);

-- drop 'id' column
alter table application_entity drop column (id);
-- add primary key which refers to 'deployment_name' column
alter table application_entity add primary key (deployment_name);

-- add foreign key to 'application_name' column which references 'deployment_name' column from application_entity table
alter table interceptor_application add constraint FK_INTERCEPTOR_APPLICATION_APPLICATION_NAME foreign key (application_name) references application_entity (deployment_name);

-- drop sequence since it's no longer used for ids generation. 'deployment_name' column becomes id and it comes from outside
drop sequence application_entity_seq;