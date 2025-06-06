create table addon_entity_aud (
  deployment_name varchar(255) not null,
  rev integer not null,
  revtype smallint,
  description varchar(2048),
  display_name varchar(255),
  endpoint varchar(255),
  forward_auth_token boolean,
  icon_url varchar(255),
  input_attachment_types varchar(255) array,
  max_input_attachments integer,
  primary key (rev, deployment_name)
);
create table application_entity_aud (
  deployment_name varchar(255) not null,
  rev integer not null,
  revtype smallint,
  application_properties varchar(255),
  defaults text,
  description varchar(2048),
  description_keywords varchar(255) array,
  display_name varchar(255),
  display_version varchar(255),
  endpoint varchar(255),
  accessible_by_per_request_key boolean,
  addons_supported boolean,
  allow_resume boolean,
  configuration_endpoint varchar(255),
  content_parts_supported boolean,
  folder_attachments_supported boolean,
  rate_endpoint varchar(255),
  seed_supported boolean,
  system_prompt_supported boolean,
  temperature_supported boolean,
  tokenize_endpoint varchar(255),
  tools_supported boolean,
  truncate_prompt_endpoint varchar(255),
  url_attachments_supported boolean,
  forward_auth_token boolean,
  icon_url varchar(255),
  input_attachment_types varchar(255) array,
  max_input_attachments integer,
  max_retry_attempts integer,
  reference varchar(255),
  application_type_schema_id varchar(255),
  primary key (rev, deployment_name)
);
create table application_type_schema_entity_aud (
  schema_id varchar(255) not null,
  rev integer not null,
  revtype smallint,
  application_type_completion_endpoint varchar(255),
  application_type_display_name varchar(255),
  application_type_editor_url varchar(255),
  application_type_viewer_url varchar(255),
  defs text,
  description varchar(2048),
  properties text,
  required varchar(255) array,
  schema varchar(255),
  primary key (rev, schema_id)
);
create table assistant_entity_aud (
  deployment_name varchar(255) not null,
  rev integer not null,
  revtype smallint,
  defaults text,
  description varchar(2048),
  description_keywords varchar(255) array,
  display_name varchar(255),
  forward_auth_token boolean,
  icon_url varchar(255),
  input_attachment_types varchar(255) array,
  max_input_attachments integer,
  primary key (rev, deployment_name)
);
create table assistants_property_entity_aud (
  id bigint not null,
  rev integer not null,
  revtype smallint,
  endpoint varchar(255),
  accessible_by_per_request_key boolean,
  addons_supported boolean,
  allow_resume boolean,
  configuration_endpoint varchar(255),
  content_parts_supported boolean,
  folder_attachments_supported boolean,
  rate_endpoint varchar(255),
  seed_supported boolean,
  system_prompt_supported boolean,
  temperature_supported boolean,
  tokenize_endpoint varchar(255),
  tools_supported boolean,
  truncate_prompt_endpoint varchar(255),
  url_attachments_supported boolean,
  primary key (rev, id)
);
create table deployment_entity_aud (
  name varchar(255) not null,
  rev integer not null,
  revtype smallint,
  default_day bigint,
  default_minute bigint,
  default_month bigint,
  default_request_day bigint,
  default_request_hour bigint,
  default_week bigint,
  is_public boolean,
  primary key (rev, name)
);
create table interceptor_application_aud (
  rev integer not null,
  interceptor_name varchar(255) not null,
  application_name varchar(255) not null,
  revtype smallint,
  primary key (
    interceptor_name, rev, application_name
  )
);
create table interceptor_entity_aud (
  name varchar(255) not null,
  rev integer not null,
  revtype smallint,
  description varchar(2048),
  display_name varchar(255),
  endpoint varchar(255),
  forward_auth_token boolean,
  icon_url varchar(255),
  primary key (rev, name)
);
create table interceptor_model_aud (
  rev integer not null,
  interceptor_name varchar(255) not null,
  model_name varchar(255) not null,
  revtype smallint,
  primary key (
    interceptor_name, rev, model_name
  )
);
create table key_entity_aud (
  name varchar(255) not null,
  rev integer not null,
  revtype smallint,
  description varchar(2048),
  KEY_VALUE varchar(255),
  project varchar(255),
  secured boolean,
  created_at_ms bigint,
  expires_at_ms bigint,
  project_contact_point text,
  key_value_generated_at_ms bigint,
  primary key (rev, name)
);
create table model_entity_aud (
  deployment_name varchar(255) not null,
  rev integer not null,
  revtype smallint,
  defaults text,
  description varchar(2048),
  display_name varchar(255),
  display_version varchar(255),
  endpoint varchar(255),
  accessible_by_per_request_key boolean,
  addons_supported boolean,
  allow_resume boolean,
  configuration_endpoint varchar(255),
  content_parts_supported boolean,
  folder_attachments_supported boolean,
  rate_endpoint varchar(255),
  seed_supported boolean,
  system_prompt_supported boolean,
  temperature_supported boolean,
  tokenize_endpoint varchar(255),
  tools_supported boolean,
  truncate_prompt_endpoint varchar(255),
  url_attachments_supported boolean,
  forward_auth_token boolean,
  icon_url varchar(255),
  input_attachment_types varchar(255) array,
  max_completion_tokens integer,
  max_prompt_tokens integer,
  max_total_tokens integer,
  max_input_attachments integer,
  max_retry_attempts integer,
  override_name varchar(255),
  completion varchar(255),
  prompt varchar(255),
  unit varchar(255),
  reference varchar(255),
  tokenizer_model varchar(255),
  topics varchar(255) array,
  type smallint check (
    type between 0
    and 2
  ),
  upstreams text,
  primary key (rev, deployment_name)
);
create table role_entity_aud (
  name varchar(255) not null,
  rev integer not null,
  revtype smallint,
  description varchar(2048),
  primary key (rev, name)
);
create table role_key_aud (
  rev integer not null,
  key_name varchar(255) not null,
  role_name varchar(255) not null,
  revtype smallint,
  primary key (key_name, rev, role_name)
);
create table role_limit_entity_aud (
  deployment_name varchar(255) not null,
  role_name varchar(255) not null,
  rev integer not null,
  revtype smallint,
  enabled boolean,
  default_day bigint,
  default_minute bigint,
  default_month bigint,
  default_request_day bigint,
  default_request_hour bigint,
  default_week bigint,
  primary key (rev, deployment_name, role_name)
);
create table route_entity_aud (
  deployment_name varchar(255) not null,
  rev integer not null,
  revtype smallint,
  description varchar(2048),
  max_retry_attempts integer,
  methods varchar(255) array,
  paths varchar(255) array,
  body varchar(255),
  status integer,
  rewrite_path boolean,
  upstreams varchar(255),
  primary key (rev, deployment_name)
);
create table revinfo (
  id integer generated by default as identity,
  author varchar(255),
  email varchar(320),
  timestamp bigint,
  primary key (id)
);

alter table if exists addon_entity_aud add constraint FK_REVINFO_ADDON_ENTITY_AUD foreign key (rev) references revinfo;
alter table if exists application_entity_aud add constraint FK_REVINFO_APPLICATION_ENTITY_AUD foreign key (rev) references revinfo;
alter table if exists application_type_schema_entity_aud add constraint FK_REVINFO_APPLICATION_TYPE_SCHEMA_ENTITY_AUD foreign key (rev) references revinfo;
alter table if exists assistant_entity_aud add constraint FK_REVINFO_ASSISTANT_ENTITY_AUD foreign key (rev) references revinfo;
alter table if exists assistants_property_entity_aud add constraint FK_REVINFO_ASSISTANTS_PROPERTY_ENTITY_AUD foreign key (rev) references revinfo;
alter table if exists deployment_entity_aud add constraint FK_REVINFO_DEPLOYMENT_ENTITY_AUD foreign key (rev) references revinfo;
alter table if exists interceptor_application_aud add constraint FK_REVINFO_INTERCEPTOR_APPLICATION_AUD foreign key (rev) references revinfo;
alter table if exists interceptor_entity_aud add constraint FK_REVINFO_INTERCEPTOR_ENTITY_AUD foreign key (rev) references revinfo;
alter table if exists interceptor_model_aud add constraint FK_REVINFO_INTERCEPTOR_MODEL_AUD foreign key (rev) references revinfo;
alter table if exists key_entity_aud add constraint FK_REVINFO_KEY_ENTITY_AUD foreign key (rev) references revinfo;
alter table if exists model_entity_aud add constraint FK_REVINFO_MODEL_ENTITY_AUD foreign key (rev) references revinfo;
alter table if exists role_entity_aud add constraint FK_REVINFO_ROLE_ENTITY_AUD foreign key (rev) references revinfo;
alter table if exists role_key_aud add constraint FK_REVINFO_ROLE_KEY_AUD foreign key (rev) references revinfo;
alter table if exists role_limit_entity_aud add constraint FK_REVINFO_ROLE_LIMIT_ENTITY_AUD foreign key (rev) references revinfo;
alter table if exists route_entity_aud add constraint FK_REVINFO_ROUTE_ENTITY_AUD foreign key (rev) references revinfo;