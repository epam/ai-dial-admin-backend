-- add responsesEndpoint field to interceptor_entity table
alter table if exists interceptor_entity add column if not exists responses_endpoint text;

-- add responsesEndpoint field to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists responses_endpoint text;
