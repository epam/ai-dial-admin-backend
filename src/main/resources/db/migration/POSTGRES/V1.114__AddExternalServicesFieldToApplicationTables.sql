-- add externalServices field to application tables
alter table if exists application_entity add column if not exists external_services text;
alter table if exists application_entity_aud add column if not exists external_services text;
