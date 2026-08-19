-- add cacheRead/cacheWrite pricing fields to model tables
alter table model_entity add cache_read nvarchar(max);
alter table model_entity add cache_write nvarchar(max);
alter table model_entity_aud add cache_read nvarchar(max);
alter table model_entity_aud add cache_write nvarchar(max);
