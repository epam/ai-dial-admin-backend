-- add intro field to application tables
alter table if exists application_entity add column if not exists intro varchar(255);
alter table if exists application_entity_aud add column if not exists intro varchar(255);

-- add intro field to model tables
alter table if exists model_entity add column if not exists intro varchar(255);
alter table if exists model_entity_aud add column if not exists intro varchar(255);

-- add intro field to tool_set tables
alter table if exists tool_set_entity add column if not exists intro varchar(255);
alter table if exists tool_set_entity_aud add column if not exists intro varchar(255);
