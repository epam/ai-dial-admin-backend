-- add validity_state_message column into key_entity table
alter table if exists key_entity add column if not exists validity_state_message text;

-- add validity_state_is_valid column into key_entity table
alter table if exists key_entity add column if not exists validity_state_is_valid boolean;
update key_entity set validity_state_is_valid = true;
alter table if exists key_entity alter column validity_state_is_valid set not null;

-- add validity_state_message column into key_entity_aud table
alter table if exists key_entity_aud add column if not exists validity_state_message text;

-- add validity_state_is_valid column into key_entity_aud table
alter table if exists key_entity_aud add column if not exists validity_state_is_valid boolean;
update key_entity_aud set validity_state_is_valid = true where revtype != 2;

-- add validity_state_message column into application_entity table
alter table if exists application_entity add column if not exists validity_state_message text;

-- add validity_state_is_valid column into application_entity table
alter table if exists application_entity add column if not exists validity_state_is_valid boolean;
update application_entity set validity_state_is_valid = true;
alter table if exists application_entity alter column validity_state_is_valid set not null;

-- add validity_state_message column into application_entity_aud table
alter table if exists application_entity_aud add column if not exists validity_state_message text;

-- add validity_state_is_valid column into application_entity_aud table
alter table if exists application_entity_aud add column if not exists validity_state_is_valid boolean;
update application_entity_aud set validity_state_is_valid = true where revtype != 2;