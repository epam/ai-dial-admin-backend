-- add SupportCommentInRateResponse field to application_entity table
alter table if exists application_entity add column if not exists support_comment_in_rate_response boolean;

-- add SupportCommentInRateResponse field to application_entity_aud table
alter table if exists application_entity_aud add column if not exists support_comment_in_rate_response boolean;

-- add SupportCommentInRateResponse field to model_entity table
alter table if exists model_entity add column if not exists support_comment_in_rate_response boolean;

-- add SupportCommentInRateResponse field to model_entity_aud table
alter table if exists model_entity_aud add column if not exists support_comment_in_rate_response boolean;

-- add SupportCommentInRateResponse field to assistants_property_entity table;
alter table if exists assistants_property_entity add column if not exists support_comment_in_rate_response boolean;

-- add SupportCommentInRateResponse field to assistants_property_entity_aud table
alter table if exists assistants_property_entity_aud add column if not exists support_comment_in_rate_response boolean;

-- add SupportCommentInRateResponse field to interceptor_entity table
alter table if exists interceptor_entity add column if not exists support_comment_in_rate_response boolean;

-- add SupportCommentInRateResponse field to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists support_comment_in_rate_response boolean;