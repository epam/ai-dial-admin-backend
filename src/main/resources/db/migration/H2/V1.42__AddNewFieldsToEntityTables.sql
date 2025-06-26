-- add new fields to role_entity table
alter table if exists role_entity add column if not exists author text;
alter table if exists role_entity add column if not exists created_at bigint;
alter table if exists role_entity add column if not exists updated_at bigint;
alter table if exists role_entity add column if not exists dependencies text array;
alter table if exists role_entity add column if not exists max_accepted_users integer;
alter table if exists role_entity add column if not exists invitation_ttl integer;

-- add new fields to role_entity_aud table
alter table if exists role_entity_aud add column if not exists author text;
alter table if exists role_entity_aud add column if not exists created_at bigint;
alter table if exists role_entity_aud add column if not exists updated_at bigint;
alter table if exists role_entity_aud add column if not exists dependencies text array;
alter table if exists role_entity_aud add column if not exists max_accepted_users integer;
alter table if exists role_entity_aud add column if not exists invitation_ttl integer;

-- create role_share_resource_limit_entity table
create table if not exists role_share_resource_limit_entity (
  deployment_name varchar(255) not null,
  role_name varchar(255) not null,
  max_accepted_users integer,
  invitation_ttl integer,
  primary key (deployment_name, role_name)
);

-- add foreign key constraints to role_share_resource_limit_entity table
alter table if exists role_share_resource_limit_entity add constraint fk_role_share_resource_limit_entity_role_name foreign key (role_name) references role_entity (name);
alter table if exists role_share_resource_limit_entity add constraint fk_role_share_resource_limit_entity_deployment_name foreign key (deployment_name) references deployment_entity (name);

-- create role_share_resource_limit_entity_aud table
create table if not exists role_share_resource_limit_entity_aud (
  deployment_name varchar(255) not null,
  role_name varchar(255) not null,
  max_accepted_users integer,
  invitation_ttl integer,
  rev integer not null,
  revtype tinyint,
  primary key (deployment_name, role_name, rev)
);

alter table if exists role_share_resource_limit_entity_aud add constraint fk_role_share_resource_limit_entity_aud_rev foreign key (rev) references revinfo;

-- add new fields to features_entity table
alter table if exists features_entity add column if not exists cache_supported boolean;
alter table if exists features_entity add column if not exists auto_caching_supported boolean;
alter table if exists features_entity add column if not exists consent_required boolean;
alter table if exists features_entity add column if not exists parallel_tool_calls_supported boolean;

-- add new fields to features_entity_aud table
alter table if exists features_entity_aud add column if not exists cache_supported boolean;
alter table if exists features_entity_aud add column if not exists auto_caching_supported boolean;
alter table if exists features_entity_aud add column if not exists consent_required boolean;
alter table if exists features_entity_aud add column if not exists parallel_tool_calls_supported boolean;

-- add new fields to application_entity table
alter table if exists application_entity add column if not exists author text;
alter table if exists application_entity add column if not exists created_at bigint;
alter table if exists application_entity add column if not exists updated_at bigint;
alter table if exists application_entity add column if not exists dependencies text array;
alter table if exists application_entity add column if not exists viewer_url text;
alter table if exists application_entity add column if not exists editor_url text;
alter table if exists application_entity add column if not exists cache_supported boolean;
alter table if exists application_entity add column if not exists auto_caching_supported boolean;
alter table if exists application_entity add column if not exists consent_required boolean;
alter table if exists application_entity add column if not exists parallel_tool_calls_supported boolean;

-- add new fields to application_entity_aud table
alter table if exists application_entity_aud add column if not exists author text;
alter table if exists application_entity_aud add column if not exists created_at bigint;
alter table if exists application_entity_aud add column if not exists updated_at bigint;
alter table if exists application_entity_aud add column if not exists dependencies text array;
alter table if exists application_entity_aud add column if not exists viewer_url text;
alter table if exists application_entity_aud add column if not exists editor_url text;
alter table if exists application_entity_aud add column if not exists cache_supported boolean;
alter table if exists application_entity_aud add column if not exists auto_caching_supported boolean;
alter table if exists application_entity_aud add column if not exists consent_required boolean;
alter table if exists application_entity_aud add column if not exists parallel_tool_calls_supported boolean;

-- add new fields to model_entity table
alter table if exists model_entity add column if not exists author text;
alter table if exists model_entity add column if not exists created_at bigint;
alter table if exists model_entity add column if not exists updated_at bigint;
alter table if exists model_entity add column if not exists dependencies text array;
alter table if exists model_entity add column if not exists fields_hashing_order text array;
alter table if exists model_entity add column if not exists cache_supported boolean;
alter table if exists model_entity add column if not exists auto_caching_supported boolean;
alter table if exists model_entity add column if not exists consent_required boolean;
alter table if exists model_entity add column if not exists parallel_tool_calls_supported boolean;

-- add new fields to model_entity_aud table
alter table if exists model_entity_aud add column if not exists author text;
alter table if exists model_entity_aud add column if not exists created_at bigint;
alter table if exists model_entity_aud add column if not exists updated_at bigint;
alter table if exists model_entity_aud add column if not exists dependencies text array;
alter table if exists model_entity_aud add column if not exists fields_hashing_order text array;
alter table if exists model_entity_aud add column if not exists cache_supported boolean;
alter table if exists model_entity_aud add column if not exists auto_caching_supported boolean;
alter table if exists model_entity_aud add column if not exists consent_required boolean;
alter table if exists model_entity_aud add column if not exists parallel_tool_calls_supported boolean;

-- add new fields to application_type_schema_entity table
alter table if exists application_type_schema_entity add column if not exists application_type_configuration_endpoint text;
alter table if exists application_type_schema_entity add column if not exists application_type_rate_endpoint text;
alter table if exists application_type_schema_entity add column if not exists application_type_tokenize_endpoint text;
alter table if exists application_type_schema_entity add column if not exists application_type_truncate_prompt_endpoint text;
alter table if exists application_type_schema_entity add column if not exists append_application_properties_header boolean;

-- add new fields to application_type_schema_entity_aud table
alter table if exists application_type_schema_entity_aud add column if not exists application_type_configuration_endpoint text;
alter table if exists application_type_schema_entity_aud add column if not exists application_type_rate_endpoint text;
alter table if exists application_type_schema_entity_aud add column if not exists application_type_tokenize_endpoint text;
alter table if exists application_type_schema_entity_aud add column if not exists application_type_truncate_prompt_endpoint text;
alter table if exists application_type_schema_entity_aud add column if not exists append_application_properties_header boolean;

-- add new fields to addon_entity table
alter table if exists addon_entity add column if not exists author text;
alter table if exists addon_entity add column if not exists created_at bigint;
alter table if exists addon_entity add column if not exists updated_at bigint;
alter table if exists addon_entity add column if not exists dependencies text array;

-- add new fields to addon_entity_aud table
alter table if exists addon_entity_aud add column if not exists author text;
alter table if exists addon_entity_aud add column if not exists created_at bigint;
alter table if exists addon_entity_aud add column if not exists updated_at bigint;
alter table if exists addon_entity_aud add column if not exists dependencies text array;

-- add new fields to assistant_entity table
alter table if exists assistant_entity add column if not exists author text;
alter table if exists assistant_entity add column if not exists created_at bigint;
alter table if exists assistant_entity add column if not exists updated_at bigint;
alter table if exists assistant_entity add column if not exists dependencies text array;

-- add new fields to assistant_entity_aud table
alter table if exists assistant_entity_aud add column if not exists author text;
alter table if exists assistant_entity_aud add column if not exists created_at bigint;
alter table if exists assistant_entity_aud add column if not exists updated_at bigint;
alter table if exists assistant_entity_aud add column if not exists dependencies text array;

-- add new fields to assistants_property_entity table
alter table if exists assistants_property_entity add column if not exists cache_supported boolean;
alter table if exists assistants_property_entity add column if not exists auto_caching_supported boolean;
alter table if exists assistants_property_entity add column if not exists consent_required boolean;
alter table if exists assistants_property_entity add column if not exists parallel_tool_calls_supported boolean;

-- add new fields to assistants_property_entity_aud table
alter table if exists assistants_property_entity_aud add column if not exists cache_supported boolean;
alter table if exists assistants_property_entity_aud add column if not exists auto_caching_supported boolean;
alter table if exists assistants_property_entity_aud add column if not exists consent_required boolean;
alter table if exists assistants_property_entity_aud add column if not exists parallel_tool_calls_supported boolean;

-- add new fields to interceptor_entity table
alter table if exists interceptor_entity add column if not exists author text;
alter table if exists interceptor_entity add column if not exists created_at bigint;
alter table if exists interceptor_entity add column if not exists updated_at bigint;
alter table if exists interceptor_entity add column if not exists dependencies text array;

-- add new fields to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists author text;
alter table if exists interceptor_entity_aud add column if not exists created_at bigint;
alter table if exists interceptor_entity_aud add column if not exists updated_at bigint;
alter table if exists interceptor_entity_aud add column if not exists dependencies text array;