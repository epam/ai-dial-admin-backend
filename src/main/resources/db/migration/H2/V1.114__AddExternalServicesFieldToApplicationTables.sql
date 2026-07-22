-- add externalServices field to application tables
alter table if exists application_entity add column if not exists external_services clob;
alter table if exists application_entity_aud add column if not exists external_services clob;
