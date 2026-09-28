-- add responsesEndpoint field to interceptor_entity table
alter table interceptor_entity add responses_endpoint nvarchar(max);

-- add responsesEndpoint field to interceptor_entity_aud table
alter table interceptor_entity_aud add responses_endpoint nvarchar(max);
