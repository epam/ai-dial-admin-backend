-- add 'role_name' column into role_key table
alter table role_key add role_name varchar(255);
-- set role_name' column values equal to 'name' column values from role_entity table joined by 'role_id' column
update role_key rk set rk.role_name = (select re.name from role_entity re where re.id = rk.role_id);
-- make 'role_name' column not null
alter table role_key alter column role_name varchar(255) not null;
-- drop 'role_id' column
alter table role_key drop column (role_id);

-- add 'role_name' column into role_limit_entity table
alter table role_limit_entity add role_name varchar(255);
-- set 'role_name' column values equal to 'name' column values from role_entity table joined by 'role_id' column
update role_limit_entity rle set rle.role_name = (select re.name from role_entity re where re.id = rle.role_id);
-- make 'role_name' column not null
alter table role_limit_entity alter column role_name varchar(255) not null;
-- drop primary key
alter table role_limit_entity drop primary key;
-- drop 'role_id' column
alter table role_limit_entity drop column (role_id);
-- add new primary key which refers to new 'role_name' column
alter table role_limit_entity add primary key (deployment_id, role_name);

-- drop 'id' column
alter table role_entity drop column (id);
-- make 'name' column not nullable
alter table role_entity alter column name varchar(255) not null;
-- add primary key which refers to 'name' column
alter table role_entity add primary key (name);

-- drop unique constraint which refers to 'name' column
alter table role_entity drop constraint UK2UQXLFG1DLWV0MTEWROKR23OU;
-- add foreign key to 'role_name' column which references 'name' column from role_entity table
alter table role_key add constraint FK_ROLE_KEY_ROLE_NAME foreign key (role_name) references role_entity (name);
-- add foreign key to 'role_name' column which references 'name' column from role_entity table
alter table role_limit_entity add constraint FK_ROLE_LIMIT_ENTITY_ROLE_NAME foreign key (role_name) references role_entity (name);

-- drop sequence since it's no longer used for ids generation. 'name' column becomes id and it comes from outside
drop sequence role_entity_seq;