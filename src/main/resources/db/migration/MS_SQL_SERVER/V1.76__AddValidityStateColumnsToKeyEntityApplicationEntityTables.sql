-- add validity_state_message column into key_entity table
alter table key_entity add validity_state_message nvarchar(max);

-- add validity_state_is_valid column into key_entity table
alter table key_entity add validity_state_is_valid bit;
go
update key_entity set validity_state_is_valid = 1;
alter table key_entity alter column validity_state_is_valid bit not null;

-- add validity_state_message column into key_entity_aud table
alter table key_entity_aud add validity_state_message nvarchar(max);

-- add validity_state_is_valid column into key_entity_aud table
alter table key_entity_aud add validity_state_is_valid bit;
go
update key_entity_aud set validity_state_is_valid = 1 where revtype != 2

-- add validity_state_message column into application_entity table
alter table application_entity add validity_state_message nvarchar(max);

-- add validity_state_is_valid column into application_entity table
alter table application_entity add validity_state_is_valid bit;
go
update application_entity set validity_state_is_valid = 1;
alter table application_entity alter column validity_state_is_valid bit not null;

-- add validity_state_message column into application_entity_aud table
alter table application_entity_aud add validity_state_message nvarchar(max);

-- add validity_state_is_valid column into application_entity_aud table
alter table application_entity_aud add validity_state_is_valid bit;
go
update application_entity_aud set validity_state_is_valid = 1 where revtype != 2;