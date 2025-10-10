-- set value for AssistantAttachmentsInRequestSupported field for old rows to application_entity table
update application_entity set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null;

-- set value for AssistantAttachmentsInRequestSupported field for old rows to application_entity_aud table
update application_entity_aud set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null and revtype != 2;

-- set value for AssistantAttachmentsInRequestSupported field for old rows to model_entity table
update model_entity set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null;

-- set value for AssistantAttachmentsInRequestSupported field for old rows to model_entity_aud table
update model_entity_aud set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null and revtype != 2;

-- set value for AssistantAttachmentsInRequestSupported field for old rows to assistants_property_entity table;
update assistants_property_entity set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null;

-- set value for AssistantAttachmentsInRequestSupported field for old rows to assistants_property_entity_aud table
update assistants_property_entity_aud set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null and revtype != 2;

-- set value for AssistantAttachmentsInRequestSupported field for old rows to interceptor_entity table
update interceptor_entity set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported;

-- set value for AssistantAttachmentsInRequestSupported field for old rows to interceptor_entity_aud table
update interceptor_entity_aud set assistant_attachments_in_request_supported = false where assistant_attachments_in_request_supported is null and revtype != 2;
