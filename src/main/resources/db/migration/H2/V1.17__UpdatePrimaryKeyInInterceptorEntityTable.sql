-- add 'interceptor_name' column into interceptor_application table
alter table interceptor_application add interceptor_name varchar(255);
-- set 'interceptor_name' column values equal to 'name' column values from interceptor_entity table joined by 'interceptor_id' column
update interceptor_application ia set ia.interceptor_name = (select ie.name from interceptor_entity ie where ie.id = ia.interceptor_id);
-- make 'interceptor_name' column not null
alter table interceptor_application alter column interceptor_name varchar(255) not null;
-- drop 'interceptor_id' column
alter table interceptor_application drop column (interceptor_id);

-- add 'interceptor_name' column into interceptor_model table
alter table interceptor_model add interceptor_name varchar(255);
-- set 'interceptor_name' column values equal to 'name' column values from interceptor_entity table joined by 'interceptor_id' column
update interceptor_model im set im.interceptor_name = (select ie.name from interceptor_entity ie where ie.id = im.interceptor_id);
-- make 'interceptor_name' column not null
alter table interceptor_model alter column interceptor_name varchar(255) not null;
-- drop 'interceptor_id' column
alter table interceptor_model drop column (interceptor_id);

-- drop 'id' column
alter table interceptor_entity drop column (id);
-- make 'name' column not nullable
alter table interceptor_entity alter column name varchar(255) not null;
-- add primary key which refers to 'name' column
alter table interceptor_entity add primary key (name);

-- drop unique constraint which refers to 'name' column
alter table interceptor_entity drop constraint UK8U9U7K211S99TJQ66IAAMHQDW;
-- add foreign key to 'interceptor_name' column which references 'name' column from interceptor_entity table
alter table interceptor_application add constraint FK_INTERCEPTOR_APPLICATION_INTERCEPTOR_NAME foreign key (interceptor_name) references interceptor_entity (name);
-- add foreign key to 'interceptor_name' column which references 'name' column from interceptor_entity table
alter table interceptor_model add constraint FK_INTERCEPTOR_MODEL_INTERCEPTOR_NAME foreign key (interceptor_name) references interceptor_entity (name);

-- drop sequence since it's no longer used for ids generation. 'name' column becomes id and it comes from outside
drop sequence interceptor_entity_seq;