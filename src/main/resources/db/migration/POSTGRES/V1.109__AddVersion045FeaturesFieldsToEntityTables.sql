-- add maxTokensSupported field to application_entity table
alter table if exists application_entity add column if not exists max_tokens_supported boolean;

-- add maxTokensSupported field to application_entity_aud table
alter table if exists application_entity_aud add column if not exists max_tokens_supported boolean;

-- add maxTokensSupported field to model_entity table
alter table if exists model_entity add column if not exists max_tokens_supported boolean;

-- add maxTokensSupported field to model_entity_aud table
alter table if exists model_entity_aud add column if not exists max_tokens_supported boolean;

-- add maxTokensSupported field to assistants_property_entity table;
alter table if exists assistants_property_entity add column if not exists max_tokens_supported boolean;

-- add maxTokensSupported field to assistants_property_entity_aud table
alter table if exists assistants_property_entity_aud add column if not exists max_tokens_supported boolean;

-- add maxTokensSupported field to interceptor_entity table
alter table if exists interceptor_entity add column if not exists max_tokens_supported boolean;

-- add maxTokensSupported field to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists max_tokens_supported boolean;

-- add maxCompletionTokensSupported field to application_entity table
alter table if exists application_entity add column if not exists max_completion_tokens_supported boolean;

-- add maxCompletionTokensSupported field to application_entity_aud table
alter table if exists application_entity_aud add column if not exists max_completion_tokens_supported boolean;

-- add maxCompletionTokensSupported field to model_entity table
alter table if exists model_entity add column if not exists max_completion_tokens_supported boolean;

-- add maxCompletionTokensSupported field to model_entity_aud table
alter table if exists model_entity_aud add column if not exists max_completion_tokens_supported boolean;

-- add maxCompletionTokensSupported field to assistants_property_entity table;
alter table if exists assistants_property_entity add column if not exists max_completion_tokens_supported boolean;

-- add maxCompletionTokensSupported field to assistants_property_entity_aud table
alter table if exists assistants_property_entity_aud add column if not exists max_completion_tokens_supported boolean;

-- add maxCompletionTokensSupported field to interceptor_entity table
alter table if exists interceptor_entity add column if not exists max_completion_tokens_supported boolean;

-- add maxCompletionTokensSupported field to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists max_completion_tokens_supported boolean;

-- add customTemperatureSupported field to application_entity table
alter table if exists application_entity add column if not exists custom_temperature_supported boolean;

-- add customTemperatureSupported field to application_entity_aud table
alter table if exists application_entity_aud add column if not exists custom_temperature_supported boolean;

-- add customTemperatureSupported field to model_entity table
alter table if exists model_entity add column if not exists custom_temperature_supported boolean;

-- add customTemperatureSupported field to model_entity_aud table
alter table if exists model_entity_aud add column if not exists custom_temperature_supported boolean;

-- add customTemperatureSupported field to assistants_property_entity table;
alter table if exists assistants_property_entity add column if not exists custom_temperature_supported boolean;

-- add customTemperatureSupported field to assistants_property_entity_aud table
alter table if exists assistants_property_entity_aud add column if not exists custom_temperature_supported boolean;

-- add customTemperatureSupported field to interceptor_entity table
alter table if exists interceptor_entity add column if not exists custom_temperature_supported boolean;

-- add customTemperatureSupported field to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists custom_temperature_supported boolean;

-- add reasoningEfforts field to application_entity table
alter table if exists application_entity add column if not exists reasoning_efforts text;

-- add reasoningEfforts field to application_entity_aud table
alter table if exists application_entity_aud add column if not exists reasoning_efforts text;

-- add reasoningEfforts field to model_entity table
alter table if exists model_entity add column if not exists reasoning_efforts text;

-- add reasoningEfforts field to model_entity_aud table
alter table if exists model_entity_aud add column if not exists reasoning_efforts text;

-- add reasoningEfforts field to assistants_property_entity table;
alter table if exists assistants_property_entity add column if not exists reasoning_efforts text;

-- add reasoningEfforts field to assistants_property_entity_aud table
alter table if exists assistants_property_entity_aud add column if not exists reasoning_efforts text;

-- add reasoningEfforts field to interceptor_entity table
alter table if exists interceptor_entity add column if not exists reasoning_efforts text;

-- add reasoningEfforts field to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists reasoning_efforts text;

-- add embeddingDimensions field to model_entity table
alter table if exists model_entity add column if not exists embedding_dimensions integer;

-- add embeddingDimensions field to model_entity_aud table
alter table if exists model_entity_aud add column if not exists embedding_dimensions integer;