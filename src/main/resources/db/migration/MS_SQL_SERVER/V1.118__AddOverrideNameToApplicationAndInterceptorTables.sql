ALTER TABLE application_entity ADD override_name nvarchar(max);
ALTER TABLE interceptor_entity ADD override_name nvarchar(max);

ALTER TABLE application_entity_aud ADD override_name nvarchar(max);
ALTER TABLE interceptor_entity_aud ADD override_name nvarchar(max);
