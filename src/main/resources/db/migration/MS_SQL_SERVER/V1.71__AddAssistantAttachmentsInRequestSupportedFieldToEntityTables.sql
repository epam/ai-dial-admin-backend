-- add AssistantAttachmentsInRequestSupported field to application_entity table
alter table application_entity add assistant_attachments_in_request_supported bit;
go
update application_entity set assistant_attachments_in_request_supported = 0 where assistant_attachments_in_request_supported is null;
-- add AssistantAttachmentsInRequestSupported field to application_entity_aud table
alter table application_entity_aud add assistant_attachments_in_request_supported bit;
go
update application_entity_aud set assistant_attachments_in_request_supported = 0 where assistant_attachments_in_request_supported is null and revtype != 2;
-- add AssistantAttachmentsInRequestSupported field to model_entity table
alter table model_entity add assistant_attachments_in_request_supported bit;
go
update model_entity set assistant_attachments_in_request_supported = 0 where assistant_attachments_in_request_supported is null;
-- add AssistantAttachmentsInRequestSupported field to model_entity_aud table
alter table model_entity_aud add assistant_attachments_in_request_supported bit;
go
update model_entity_aud set assistant_attachments_in_request_supported = 0 where assistant_attachments_in_request_supported is null and revtype != 2;
-- add AssistantAttachmentsInRequestSupported field to assistants_property_entity table;
alter table assistants_property_entity add assistant_attachments_in_request_supported bit;
go
update assistants_property_entity set assistant_attachments_in_request_supported = 0 where assistant_attachments_in_request_supported is null;
-- add AssistantAttachmentsInRequestSupported field to assistants_property_entity_aud table
alter table assistants_property_entity_aud add assistant_attachments_in_request_supported bit;
go
update assistants_property_entity_aud set assistant_attachments_in_request_supported = 0 where assistant_attachments_in_request_supported is null and revtype != 2;
-- add AssistantAttachmentsInRequestSupported field to interceptor_entity table
alter table interceptor_entity add assistant_attachments_in_request_supported bit;
go
update interceptor_entity set assistant_attachments_in_request_supported = 0 where assistant_attachments_in_request_supported is null;
-- add AssistantAttachmentsInRequestSupported field to interceptor_entity_aud table
alter table interceptor_entity_aud add assistant_attachments_in_request_supported bit;
go
update interceptor_entity_aud set assistant_attachments_in_request_supported = 0 where assistant_attachments_in_request_supported is null and revtype != 2;
