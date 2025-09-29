alter table if exists key_entity add column if not exists validity_state_message varchar(1024);
alter table if exists key_entity add column if not exists validity_state_is_valid boolean not null;
alter table if exists key_entity_aud add column if not exists validity_state_message varchar(1024);
alter table if exists key_entity_aud add column if not exists validity_state_is_valid boolean;

alter table if exists application_entity add column if not exists validity_state_message varchar(1024);
alter table if exists application_entity add column if not exists validity_state_is_valid boolean not null;
alter table if exists application_entity_aud add column if not exists validity_state_message varchar(1024);
alter table if exists application_entity_aud add column if not exists validity_state_is_valid boolean;