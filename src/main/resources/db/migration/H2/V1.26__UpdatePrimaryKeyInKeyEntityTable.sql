-- add 'name' column into key_entity table
alter table key_entity add name varchar(255);
-- set 'name' column values equal to 'key_name' column values
update key_entity set name = key_name;

-- add 'key_name' column into role_key table
alter table role_key add key_name varchar(255);
-- set 'key_name' column values equal to 'name' column values from key_entity table joined by 'key_id' column
update role_key rk set rk.key_name = (select ke.name from key_entity ke where ke.id = rk.key_id);
-- make 'key_name' column not null
alter table role_key alter column key_name varchar(255) not null;
-- drop 'key_id' column
alter table role_key drop column (key_id);

-- drop 'id' column
alter table key_entity drop column (id);
-- make 'name' column not null
alter table key_entity alter column name varchar(255) not null;
-- add primary key which refers to 'name' column
alter table key_entity add primary key (name);

-- drop unique constraint which refers to 'key_name' column
alter table key_entity drop constraint UKIWN1LK6LCML3AIHEFBB48V5MW;
-- rename 'key_name' column to 'key_value' column
alter table key_entity rename column key_name to "KEY_VALUE";
-- add unique constraint which refers to 'key_value' column
alter table key_entity add constraint UK_KEY_ENTITY_KEY_VALUE unique (key_value);
-- add foreign key to 'key_name' column which references 'name' column from key_entity table
alter table role_key add constraint FK_ROLE_KEY_KEY_NAME foreign key (key_name) references key_entity (name);

-- drop sequence since it's no longer used for ids generation. 'name' column becomes id and it comes from outside
drop sequence key_entity_seq;