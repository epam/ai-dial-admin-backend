alter table key_entity add validity_state_message nvarchar(1024);
alter table key_entity add validity_state_is_valid bit not null;
alter table key_entity_aud add validity_state_message nvarchar(1024);
alter table key_entity_aud add validity_state_is_valid bit;

alter table application_entity add validity_state_message nvarchar(1024);
alter table application_entity add validity_state_is_valid bit not null;
alter table application_entity_aud add validity_state_message nvarchar(1024);
alter table application_entity_aud add validity_state_is_valid bit;