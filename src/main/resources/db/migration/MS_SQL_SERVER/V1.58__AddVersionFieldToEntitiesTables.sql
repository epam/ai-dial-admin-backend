-- add new field version to model_entity table
alter table model_entity add version bigint not null default 0;

