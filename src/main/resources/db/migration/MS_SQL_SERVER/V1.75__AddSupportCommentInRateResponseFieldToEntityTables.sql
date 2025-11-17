-- add SupportCommentInRateResponse field to application_entity table
alter table application_entity add support_comment_in_rate_response bit;

-- add SupportCommentInRateResponse field to application_entity_aud table
alter table application_entity_aud add support_comment_in_rate_response bit;

-- add SupportCommentInRateResponse field to model_entity table
alter table model_entity add support_comment_in_rate_response bit;

-- add SupportCommentInRateResponse field to model_entity_aud table
alter table model_entity_aud add support_comment_in_rate_response bit;

-- add SupportCommentInRateResponse field to assistants_property_entity table;
alter table assistants_property_entity add support_comment_in_rate_response bit;

-- add SupportCommentInRateResponse field to assistants_property_entity_aud table
alter table assistants_property_entity_aud add support_comment_in_rate_response bit;

-- add SupportCommentInRateResponse field to interceptor_entity table
alter table interceptor_entity add support_comment_in_rate_response bit;

-- add SupportCommentInRateResponse field to interceptor_entity_aud table
alter table interceptor_entity_aud add support_comment_in_rate_response bit;