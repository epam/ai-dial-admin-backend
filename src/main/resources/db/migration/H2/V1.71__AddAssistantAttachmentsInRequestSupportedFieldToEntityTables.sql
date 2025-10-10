-- add AssistantAttachmentsInRequestSupported field to application_entity table
alter table if exists application_entity add column if not exists assistant_attachments_in_request_supported boolean;
update application_entity set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null;

-- add AssistantAttachmentsInRequestSupported field to application_entity_aud table
alter table if exists application_entity_aud add column if not exists assistant_attachments_in_request_supported boolean ;
update application_entity_aud set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null and revtype != 2;

-- add AssistantAttachmentsInRequestSupported field to model_entity table
alter table if exists model_entity add column if not exists assistant_attachments_in_request_supported boolean;
update model_entity set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null;

-- add AssistantAttachmentsInRequestSupported field to model_entity_aud table
alter table if exists model_entity_aud add column if not exists assistant_attachments_in_request_supported boolean;
update model_entity_aud set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null and revtype != 2;

-- add AssistantAttachmentsInRequestSupported field to assistants_property_entity table;
alter table if exists assistants_property_entity add column if not exists assistant_attachments_in_request_supported boolean;
update assistants_property_entity set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null;

-- add AssistantAttachmentsInRequestSupported field to assistants_property_entity_aud table
alter table if exists assistants_property_entity_aud add column if not exists assistant_attachments_in_request_supported boolean;
update assistants_property_entity_aud set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null and revtype != 2;

-- add AssistantAttachmentsInRequestSupported field to interceptor_entity table
alter table if exists interceptor_entity add column if not exists assistant_attachments_in_request_supported boolean ;
update interceptor_entity set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported;

-- add AssistantAttachmentsInRequestSupported field to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists assistant_attachments_in_request_supported boolean;
update interceptor_entity_aud set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null and revtype != 2;
