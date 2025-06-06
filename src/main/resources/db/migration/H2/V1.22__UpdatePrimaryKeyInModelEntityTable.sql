-- add 'model_name' column into interceptor_model table
alter table interceptor_model add model_name varchar(255);
-- set 'model_name' column values equal to 'deployment_name' column values from model_entity table joined by 'model_id' column
update interceptor_model im set im.model_name = (select me.deployment_name from model_entity me where me.id = im.model_id);
-- make 'model_name' column not null
alter table interceptor_model alter column model_name varchar(255) not null;
-- drop 'model_id' column
alter table interceptor_model drop column (model_id);

-- drop 'id' column
alter table model_entity drop column (id);
-- add primary key which refers to 'deployment_name' column
alter table model_entity add primary key (deployment_name);

-- add foreign key to 'model_name' column which references 'deployment_name' column from model_entity table
alter table interceptor_model add constraint FK_INTERCEPTOR_MODEL_MODEL_NAME foreign key (model_name) references model_entity (deployment_name);

-- drop sequence since it's no longer used for ids generation. 'deployment_name' column becomes id and it comes from outside
drop sequence model_entity_seq;