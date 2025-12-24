update model_entity set endpoint = null where adapter_name is not null;
update model_entity_aud set endpoint = null where adapter_name is not null;