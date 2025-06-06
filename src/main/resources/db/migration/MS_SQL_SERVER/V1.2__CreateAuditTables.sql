create table addon_entity_aud (
  deployment_name nvarchar(255) not null,
  rev integer not null,
  revtype smallint,
  description nvarchar(max),
  display_name nvarchar(max),
  endpoint nvarchar(max),
  forward_auth_token bit,
  icon_url nvarchar(max),
  input_attachment_types varbinary(max),
  max_input_attachments integer,
  primary key (rev, deployment_name)
);
create table application_entity_aud (
  deployment_name nvarchar(255) not null,
  rev integer not null,
  revtype smallint,
  application_properties nvarchar(max),
  defaults nvarchar(max),
  description nvarchar(max),
  description_keywords varbinary(max),
  display_name nvarchar(255),
  display_version nvarchar(255),
  endpoint nvarchar(max),
  accessible_by_per_request_key bit,
  addons_supported bit,
  allow_resume bit,
  configuration_endpoint nvarchar(max),
  content_parts_supported bit,
  folder_attachments_supported bit,
  rate_endpoint nvarchar(max),
  seed_supported bit,
  system_prompt_supported bit,
  temperature_supported bit,
  tokenize_endpoint nvarchar(max),
  tools_supported bit,
  truncate_prompt_endpoint nvarchar(max),
  url_attachments_supported bit,
  forward_auth_token bit,
  icon_url nvarchar(max),
  input_attachment_types varbinary(max),
  max_input_attachments integer,
  max_retry_attempts integer,
  reference nvarchar(max),
  application_type_schema_id nvarchar(850),
  primary key (rev, deployment_name)
);
create table application_type_schema_entity_aud (
  schema_id nvarchar(850) not null,
  rev integer not null,
  revtype smallint,
  application_type_completion_endpoint nvarchar(max),
  application_type_display_name nvarchar(max),
  application_type_editor_url nvarchar(max),
  application_type_viewer_url nvarchar(max),
  defs nvarchar(max),
  description nvarchar(max),
  properties nvarchar(max),
  required varbinary(max),
  [schema] nvarchar(max),
  primary key (rev, schema_id)
);
create table assistant_entity_aud (
  deployment_name nvarchar(255) not null,
  rev integer not null,
  revtype smallint,
  defaults nvarchar(max),
  description nvarchar(max),
  description_keywords varbinary(max),
  display_name nvarchar(max),
  forward_auth_token bit,
  icon_url nvarchar(max),
  input_attachment_types varbinary(max),
  max_input_attachments integer,
  primary key (rev, deployment_name)
);
create table assistants_property_entity_aud (
  id bigint not null,
  rev integer not null,
  revtype smallint,
  endpoint nvarchar(max),
  rate_endpoint nvarchar(max),
  tokenize_endpoint nvarchar(max),
  truncate_prompt_endpoint nvarchar(max),
  configuration_endpoint nvarchar(max),
  system_prompt_supported bit,
  tools_supported bit,
  seed_supported bit,
  url_attachments_supported bit,
  folder_attachments_supported bit,
  allow_resume bit,
  accessible_by_per_request_key bit,
  content_parts_supported bit,
  temperature_supported bit,
  addons_supported bit,
  primary key (rev, id)
);
create table deployment_entity_aud (
  name nvarchar(255) not null,
  rev integer not null,
  revtype smallint,
  default_day bigint,
  default_minute bigint,
  default_month bigint,
  default_request_day bigint,
  default_request_hour bigint,
  default_week bigint,
  is_public bit,
  primary key (rev, name)
);
create table interceptor_application_aud (
  rev integer not null,
  interceptor_name nvarchar(255) not null,
  application_name nvarchar(255) not null,
  revtype smallint,
  primary key (
    interceptor_name, rev, application_name
  )
);
create table interceptor_entity_aud (
  name nvarchar(255) not null,
  rev integer not null,
  revtype smallint,
  description nvarchar(max),
  display_name nvarchar(max),
  endpoint nvarchar(max),
  forward_auth_token bit,
  icon_url nvarchar(max),
  primary key (rev, name)
);
create table interceptor_model_aud (
  rev integer not null,
  interceptor_name nvarchar(255) not null,
  model_name nvarchar(255) not null,
  revtype smallint,
  primary key (interceptor_name, rev, model_name)
);
create table key_entity_aud (
  name nvarchar(255) not null,
  rev integer not null,
  revtype smallint,
  description nvarchar(max),
  key_value nvarchar(255),
  project nvarchar(max),
  secured bit,
  created_at_ms bigint,
  expires_at_ms bigint,
  project_contact_point text,
  key_value_generated_at_ms bigint,
  primary key (rev, name)
);
create table model_entity_aud (
  deployment_name nvarchar(255) not null,
  rev integer not null,
  revtype smallint,
  defaults nvarchar(max),
  description nvarchar(max),
  display_name nvarchar(255),
  display_version nvarchar(255),
  endpoint nvarchar(max),
  accessible_by_per_request_key bit,
  addons_supported bit,
  allow_resume bit,
  configuration_endpoint nvarchar(max),
  content_parts_supported bit,
  folder_attachments_supported bit,
  rate_endpoint nvarchar(max),
  seed_supported bit,
  system_prompt_supported bit,
  temperature_supported bit,
  tokenize_endpoint nvarchar(max),
  tools_supported bit,
  truncate_prompt_endpoint nvarchar(max),
  url_attachments_supported bit,
  forward_auth_token bit,
  icon_url nvarchar(max),
  input_attachment_types varbinary(max),
  max_completion_tokens integer,
  max_prompt_tokens integer,
  max_total_tokens integer,
  max_input_attachments integer,
  max_retry_attempts integer,
  override_name nvarchar(max),
  completion nvarchar(max),
  prompt nvarchar(max),
  unit nvarchar(max),
  reference nvarchar(max),
  tokenizer_model nvarchar(max),
  topics varbinary(max),
  type smallint check (
    type between 0
    and 2
  ),
  upstreams nvarchar(max),
  primary key (rev, deployment_name)
);
create table role_entity_aud (
  name nvarchar(255) not null,
  rev integer not null,
  revtype smallint,
  description nvarchar(max),
  primary key (rev, name)
);
create table role_key_aud (
  rev integer not null,
  key_name nvarchar(255) not null,
  role_name nvarchar(255) not null,
  revtype smallint,
  primary key (key_name, rev, role_name)
);
create table role_limit_entity_aud (
  deployment_name nvarchar(255) not null,
  role_name nvarchar(255) not null,
  rev integer not null,
  revtype smallint,
  enabled bit,
  default_day bigint,
  default_minute bigint,
  default_month bigint,
  default_request_day bigint,
  default_request_hour bigint,
  default_week bigint,
  primary key (rev, deployment_name, role_name)
);
create table route_entity_aud (
  deployment_name nvarchar(255) not null,
  rev integer not null,
  revtype smallint,
  description nvarchar(max),
  max_retry_attempts integer,
  methods varbinary(255),
  paths varbinary(max),
  body nvarchar(max),
  status integer,
  rewrite_path bit,
  upstreams nvarchar(max),
  primary key (rev, deployment_name)
);
create table revinfo (
  id integer IDENTITY(1,1),
  author nvarchar(255),
  email nvarchar(320),
  timestamp bigint,
  primary key (id)
);

alter table addon_entity_aud add constraint FK_REVINFO_ADDON_ENTITY_AUD foreign key (rev) references revinfo;
alter table application_entity_aud add constraint FK_REVINFO_APPLICATION_ENTITY_AUD foreign key (rev) references revinfo;
alter table application_type_schema_entity_aud add constraint FK_REVINFO_APPLICATION_TYPE_SCHEMA_ENTITY_AUD foreign key (rev) references revinfo;
alter table assistant_entity_aud add constraint FK_REVINFO_ASSISTANT_ENTITY_AUD foreign key (rev) references revinfo;
alter table assistants_property_entity_aud add constraint FK_REVINFO_ASSISTANTS_PROPERTY_ENTITY_AUD foreign key (rev) references revinfo;
alter table deployment_entity_aud add constraint FK_REVINFO_DEPLOYMENT_ENTITY_AUD foreign key (rev) references revinfo;
alter table interceptor_application_aud add constraint FK_REVINFO_INTERCEPTOR_APPLICATION_AUD foreign key (rev) references revinfo;
alter table interceptor_entity_aud add constraint FK_REVINFO_INTERCEPTOR_ENTITY_AUD foreign key (rev) references revinfo;
alter table interceptor_model_aud add constraint FK_REVINFO_INTERCEPTOR_MODEL_AUD foreign key (rev) references revinfo;
alter table key_entity_aud add constraint FK_REVINFO_KEY_ENTITY_AUD foreign key (rev) references revinfo;
alter table model_entity_aud add constraint FK_REVINFO_MODEL_ENTITY_AUD foreign key (rev) references revinfo;
alter table role_entity_aud add constraint FK_REVINFO_ROLE_ENTITY_AUD foreign key (rev) references revinfo;
alter table role_key_aud add constraint FK_REVINFO_ROLE_KEY_AUD foreign key (rev) references revinfo;
alter table role_limit_entity_aud add constraint FK_REVINFO_ROLE_LIMIT_ENTITY_AUD foreign key (rev) references revinfo;
alter table route_entity_aud add constraint FK_REVINFO_ROUTE_ENTITY_AUD foreign key (rev) references revinfo;