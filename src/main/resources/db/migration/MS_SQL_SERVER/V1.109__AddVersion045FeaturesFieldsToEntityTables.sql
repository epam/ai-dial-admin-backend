-- add maxTokensSupported field to application_entity table
alter table application_entity add max_tokens_supported bit;

-- add maxTokensSupported field to application_entity_aud table
alter table application_entity_aud add max_tokens_supported bit;

-- add maxTokensSupported field to model_entity table
alter table model_entity add max_tokens_supported bit;

-- add maxTokensSupported field to model_entity_aud table
alter table model_entity_aud add max_tokens_supported bit;

-- add maxTokensSupported field to assistants_property_entity table;
alter table assistants_property_entity add max_tokens_supported bit;

-- add maxTokensSupported field to assistants_property_entity_aud table
alter table assistants_property_entity_aud add max_tokens_supported bit;

-- add maxTokensSupported field to interceptor_entity table
alter table interceptor_entity add max_tokens_supported bit;

-- add maxTokensSupported field to interceptor_entity_aud table
alter table interceptor_entity_aud add max_tokens_supported bit;

-- add maxCompletionTokensSupported field to application_entity table
alter table application_entity add max_completion_tokens_supported bit;

-- add maxCompletionTokensSupported field to application_entity_aud table
alter table application_entity_aud add max_completion_tokens_supported bit;

-- add maxCompletionTokensSupported field to model_entity table
alter table model_entity add max_completion_tokens_supported bit;

-- add maxCompletionTokensSupported field to model_entity_aud table
alter table model_entity_aud add max_completion_tokens_supported bit;

-- add maxCompletionTokensSupported field to assistants_property_entity table;
alter table assistants_property_entity add max_completion_tokens_supported bit;

-- add maxCompletionTokensSupported field to assistants_property_entity_aud table
alter table assistants_property_entity_aud add max_completion_tokens_supported bit;

-- add maxCompletionTokensSupported field to interceptor_entity table
alter table interceptor_entity add max_completion_tokens_supported bit;

-- add maxCompletionTokensSupported field to interceptor_entity_aud table
alter table interceptor_entity_aud add max_completion_tokens_supported bit;

-- add customTemperatureSupported field to application_entity table
alter table application_entity add custom_temperature_supported bit;

-- add customTemperatureSupported field to application_entity_aud table
alter table application_entity_aud add custom_temperature_supported bit;

-- add customTemperatureSupported field to model_entity table
alter table model_entity add custom_temperature_supported bit;

-- add customTemperatureSupported field to model_entity_aud table
alter table model_entity_aud add custom_temperature_supported bit;

-- add customTemperatureSupported field to assistants_property_entity table;
alter table assistants_property_entity add custom_temperature_supported bit;

-- add customTemperatureSupported field to assistants_property_entity_aud table
alter table assistants_property_entity_aud add custom_temperature_supported bit;

-- add customTemperatureSupported field to interceptor_entity table
alter table interceptor_entity add custom_temperature_supported bit;

-- add customTemperatureSupported field to interceptor_entity_aud table
alter table interceptor_entity_aud add custom_temperature_supported bit;

-- add reasoningEffortsSupported field to application_entity table
alter table application_entity add reasoning_efforts_supported bit;

-- add reasoningEffortsSupported field to application_entity_aud table
alter table application_entity_aud add reasoning_efforts_supported bit;

-- add reasoningEffortsSupported field to model_entity table
alter table model_entity add reasoning_efforts_supported bit;

-- add reasoningEffortsSupported field to model_entity_aud table
alter table model_entity_aud add reasoning_efforts_supported bit;

-- add reasoningEffortsSupported field to assistants_property_entity table;
alter table assistants_property_entity add reasoning_efforts_supported bit;

-- add reasoningEffortsSupported field to assistants_property_entity_aud table
alter table assistants_property_entity_aud add reasoning_efforts_supported bit;

-- add reasoningEffortsSupported field to interceptor_entity table
alter table interceptor_entity add reasoning_efforts_supported bit;

-- add reasoningEffortsSupported field to interceptor_entity_aud table
alter table interceptor_entity_aud add reasoning_efforts_supported bit;