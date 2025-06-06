-- add new fields to role_entity table
alter table role_entity add author text;
alter table role_entity add created_at bigint;
alter table role_entity add updated_at bigint;
alter table role_entity add dependencies varbinary(max);
alter table role_entity add max_accepted_users integer;
alter table role_entity add invitation_ttl integer;

-- add new fields to role_entity_aud table
alter table role_entity_aud add author text;
alter table role_entity_aud add created_at bigint;
alter table role_entity_aud add updated_at bigint;
alter table role_entity_aud add dependencies varbinary(max);
alter table role_entity_aud add max_accepted_users integer;
alter table role_entity_aud add invitation_ttl integer;

-- create role_share_resource_limit_entity table
create table role_share_resource_limit_entity (
  deployment_name nvarchar(255) not null,
  role_name nvarchar(255) not null,
  max_accepted_users integer,
  invitation_ttl integer,
  primary key (deployment_name, role_name)
);

-- add foreign key constraints to role_share_resource_limit_entity table
alter table role_share_resource_limit_entity add constraint fk_role_share_resource_limit_entity_role_name foreign key (role_name) references role_entity (name);
alter table role_share_resource_limit_entity add constraint fk_role_share_resource_limit_entity_deployment_name foreign key (deployment_name) references deployment_entity (name);

-- create role_share_resource_limit_entity_aud table
create table role_share_resource_limit_entity_aud (
  deployment_name nvarchar(255) not null,
  role_name nvarchar(255) not null,
  max_accepted_users integer,
  invitation_ttl integer,
  rev integer not null,
  revtype smallint,
  primary key (deployment_name, role_name, rev)
);

alter table role_share_resource_limit_entity_aud add constraint fk_role_share_resource_limit_entity_aud_rev foreign key (rev) references revinfo;

-- add new fields to application_entity table
alter table application_entity add author text;
alter table application_entity add created_at bigint;
alter table application_entity add updated_at bigint;
alter table application_entity add dependencies varbinary(max);
alter table application_entity add viewer_url text;
alter table application_entity add editor_url text;
alter table application_entity add cache_supported bit;
alter table application_entity add auto_caching_supported bit;
alter table application_entity add consent_required bit;
alter table application_entity add parallel_tool_calls_supported bit;

-- add new fields to application_entity_aud table
alter table application_entity_aud add author text;
alter table application_entity_aud add created_at bigint;
alter table application_entity_aud add updated_at bigint;
alter table application_entity_aud add dependencies varbinary(max);
alter table application_entity_aud add viewer_url text;
alter table application_entity_aud add editor_url text;
alter table application_entity_aud add cache_supported bit;
alter table application_entity_aud add auto_caching_supported bit;
alter table application_entity_aud add consent_required bit;
alter table application_entity_aud add parallel_tool_calls_supported bit;

-- add new fields to model_entity table
alter table model_entity add author text;
alter table model_entity add created_at bigint;
alter table model_entity add updated_at bigint;
alter table model_entity add dependencies varbinary(max);
alter table model_entity add fields_hashing_order varbinary(max);
alter table model_entity add cache_supported bit;
alter table model_entity add auto_caching_supported bit;
alter table model_entity add consent_required bit;
alter table model_entity add parallel_tool_calls_supported bit;

-- add new fields to model_entity_aud table
alter table model_entity_aud add author text;
alter table model_entity_aud add created_at bigint;
alter table model_entity_aud add updated_at bigint;
alter table model_entity_aud add dependencies varbinary(max);
alter table model_entity_aud add fields_hashing_order varbinary(max);
alter table model_entity_aud add cache_supported bit;
alter table model_entity_aud add auto_caching_supported bit;
alter table model_entity_aud add consent_required bit;
alter table model_entity_aud add parallel_tool_calls_supported bit;

-- add new fields to application_type_schema_entity table
alter table application_type_schema_entity add application_type_configuration_endpoint text;
alter table application_type_schema_entity add application_type_rate_endpoint text;
alter table application_type_schema_entity add application_type_tokenize_endpoint text;
alter table application_type_schema_entity add application_type_truncate_prompt_endpoint text;
alter table application_type_schema_entity add append_application_properties_header bit;

-- add new fields to application_type_schema_entity_aud table
alter table application_type_schema_entity_aud add application_type_configuration_endpoint text;
alter table application_type_schema_entity_aud add application_type_rate_endpoint text;
alter table application_type_schema_entity_aud add application_type_tokenize_endpoint text;
alter table application_type_schema_entity_aud add application_type_truncate_prompt_endpoint text;
alter table application_type_schema_entity_aud add append_application_properties_header bit;

-- add new fields to addon_entity table
alter table addon_entity add author text;
alter table addon_entity add created_at bigint;
alter table addon_entity add updated_at bigint;
alter table addon_entity add dependencies varbinary(max);

-- add new fields to addon_entity_aud table
alter table addon_entity_aud add author text;
alter table addon_entity_aud add created_at bigint;
alter table addon_entity_aud add updated_at bigint;
alter table addon_entity_aud add dependencies varbinary(max);

-- add new fields to assistant_entity table
alter table assistant_entity add author text;
alter table assistant_entity add created_at bigint;
alter table assistant_entity add updated_at bigint;
alter table assistant_entity add dependencies varbinary(max);

-- add new fields to assistant_entity_aud table
alter table assistant_entity_aud add author text;
alter table assistant_entity_aud add created_at bigint;
alter table assistant_entity_aud add updated_at bigint;
alter table assistant_entity_aud add dependencies varbinary(max);

-- add new fields to assistants_property_entity table
alter table assistants_property_entity add cache_supported bit;
alter table assistants_property_entity add auto_caching_supported bit;
alter table assistants_property_entity add consent_required bit;
alter table assistants_property_entity add parallel_tool_calls_supported bit;

-- add new fields to assistants_property_entity_aud table
alter table assistants_property_entity_aud add cache_supported bit;
alter table assistants_property_entity_aud add auto_caching_supported bit;
alter table assistants_property_entity_aud add consent_required bit;
alter table assistants_property_entity_aud add parallel_tool_calls_supported bit;

-- add new fields to interceptor_entity table
alter table interceptor_entity add author text;
alter table interceptor_entity add created_at bigint;
alter table interceptor_entity add updated_at bigint;
alter table interceptor_entity add dependencies varbinary(max);

-- add new fields to interceptor_entity_aud table
alter table interceptor_entity_aud add author text;
alter table interceptor_entity_aud add created_at bigint;
alter table interceptor_entity_aud add updated_at bigint;
alter table interceptor_entity_aud add dependencies varbinary(max);