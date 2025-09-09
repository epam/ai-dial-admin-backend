-- Update model_entity
UPDATE model_entity
SET adapter_completion_endpoint_path =
    COALESCE(endpoint_deployment_name, '') ||
    CASE
        WHEN type = 0 THEN '/chat/completions'
        ELSE '/embeddings'
    END
WHERE adapter_name IS NOT NULL
  AND adapter_completion_endpoint_path IS NULL;

-- Update model_entity_aud
UPDATE model_entity_aud
SET adapter_completion_endpoint_path =
    COALESCE(endpoint_deployment_name, '') ||
    CASE
        WHEN type = 0 THEN '/chat/completions'
        ELSE '/embeddings'
    END
WHERE adapter_name IS NOT NULL
  AND adapter_completion_endpoint_path IS NULL;
