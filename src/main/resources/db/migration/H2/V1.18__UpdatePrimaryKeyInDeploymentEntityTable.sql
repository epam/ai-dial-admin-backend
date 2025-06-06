-- add 'deployment_name' column into addon_entity table
alter table addon_entity add deployment_name varchar(255);
-- set 'deployment_name' column values equal to 'name' column values from deployment_entity table joined by 'deployment_id' column
update addon_entity ae set ae.deployment_name = (select de.name from deployment_entity de where de.id = ae.deployment_id);
-- make 'deployment_name' column not null
alter table addon_entity alter column deployment_name varchar(255) not null;
-- drop 'deployment_id' column
alter table addon_entity drop column (deployment_id);

-- add 'deployment_name' column into application_entity table
alter table application_entity add deployment_name varchar(255);
-- set 'deployment_name' column values equal to 'name' column values from deployment_entity table joined by 'deployment_id' column
update application_entity ae set ae.deployment_name = (select de.name from deployment_entity de where de.id = ae.deployment_id);
-- make 'deployment_name' column not null
alter table application_entity alter column deployment_name varchar(255) not null;
-- drop 'deployment_id' column
alter table application_entity drop column (deployment_id);

-- add 'deployment_name' column into assistant_entity table
alter table assistant_entity add deployment_name varchar(255);
-- set 'deployment_name' column values equal to 'name' column values from deployment_entity table joined by 'deployment_id' column
update assistant_entity ae set ae.deployment_name = (select de.name from deployment_entity de where de.id = ae.deployment_id);
-- make 'deployment_name' column not null
alter table assistant_entity alter column deployment_name varchar(255) not null;
-- drop 'deployment_id' column
alter table assistant_entity drop column (deployment_id);

-- add 'deployment_name' column into model_entity table
alter table model_entity add deployment_name varchar(255);
-- set 'deployment_name' column values equal to 'name' column values from deployment_entity table joined by 'deployment_id' column
update model_entity me set me.deployment_name = (select de.name from deployment_entity de where de.id = me.deployment_id);
-- make 'deployment_name' column not null
alter table model_entity alter column deployment_name varchar(255) not null;
-- drop 'deployment_id' column
alter table model_entity drop column (deployment_id);

-- add 'deployment_name' column into route_entity table
alter table route_entity add deployment_name varchar(255);
-- set 'deployment_name' column values equal to 'name' column values from deployment_entity table joined by 'deployment_id' column
update route_entity re set re.deployment_name = (select de.name from deployment_entity de where de.id = re.deployment_id);
-- make 'deployment_name' column not null
alter table route_entity alter column deployment_name varchar(255) not null;
-- drop 'deployment_id' column
alter table route_entity drop column (deployment_id);

-- add 'deployment_name' column into role_limit_entity table
alter table role_limit_entity add deployment_name varchar(255);
-- set 'deployment_name' column values equal to 'name' column values from deployment_entity table joined by 'deployment_id' column
update role_limit_entity rle set rle.deployment_name = (select de.name from deployment_entity de where de.id = rle.deployment_id);
-- make 'deployment_name' column not null
alter table role_limit_entity alter column deployment_name varchar(255) not null;
-- drop primary key
alter table role_limit_entity drop primary key;
-- drop 'deployment_id' column
alter table role_limit_entity drop column (deployment_id);
-- add new primary key which refers to new 'deployment_name' column
alter table role_limit_entity add primary key (deployment_name, role_name);

-- drop 'id' column
alter table deployment_entity drop column (id);
-- make 'name' column not nullable
alter table deployment_entity alter column name varchar(255) not null;
-- add primary key which refers to 'name' column
alter table deployment_entity add primary key (name);

-- drop unique constraint which refers to 'name' column
alter table deployment_entity drop constraint UK5M4RGDMP7MLEWC6WDAPG00D8G;
-- add foreign key to 'deployment_name' column which references 'name' column from deployment_entity table
alter table addon_entity add constraint FK_ADDON_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);
-- add foreign key to 'deployment_name' column which references 'name' column from deployment_entity table
alter table application_entity add constraint FK_APPLICATION_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);
-- add foreign key to 'deployment_name' column which references 'name' column from deployment_entity table
alter table assistant_entity add constraint FK_ASSISTANT_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);
-- add foreign key to 'deployment_name' column which references 'name' column from deployment_entity table
alter table model_entity add constraint FK_MODEL_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);
-- add foreign key to 'deployment_name' column which references 'name' column from deployment_entity table
alter table route_entity add constraint FK_ROUTE_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);
-- add foreign key to 'deployment_name' column which references 'name' column from deployment_entity table
alter table role_limit_entity add constraint FK_ROLE_LIMIT_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);

-- drop sequence since it's no longer used for ids generation. 'name' column becomes id and it comes from outside
drop sequence deployment_entity_seq;