-- add new field version to adapter_entity table
alter table if exists adapter_entity add column if not exists version bigint not null default 0;

-- add new field version to model_entity table
alter table if exists model_entity add column if not exists version bigint not null default 0;

-- add new field version to role_entity table
alter table if exists role_entity add column if not exists version bigint not null default 0;

-- add new field version to role_entity table
alter table if exists route_entity add column if not exists version bigint not null default 0;

-- add new field version to addon_entity table
alter table if exists addon_entity add column if not exists version bigint not null default 0;

-- add new field version to application_entity table
alter table if exists application_entity add column if not exists version bigint not null default 0;

-- add new field version to key_entity table
alter table if exists key_entity add column if not exists version bigint not null default 0;
