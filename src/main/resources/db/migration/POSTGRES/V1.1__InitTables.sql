create table deployment_entity (
  name varchar(255) not null,
  default_day bigint,
  default_minute bigint,
  default_month bigint,
  default_request_day bigint,
  default_request_hour bigint,
  default_week bigint,
  is_public boolean,
  primary key (name)
);

create table role_entity (
  name varchar(255) not null,
  description varchar(2048),
  primary key (name)
);

create table addon_entity (
  deployment_name varchar(255) not null,
  description varchar(2048),
  display_name varchar(255),
  endpoint varchar(255),
  forward_auth_token boolean,
  icon_url varchar(255),
  input_attachment_types varchar(255) array,
  max_input_attachments integer,
  primary key (deployment_name)
);
alter table addon_entity add constraint FK_ADDON_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);

create table application_type_schema_entity (
  schema_id varchar(255) not null,
  application_type_completion_endpoint varchar(255),
  application_type_display_name varchar(255),
  application_type_editor_url varchar(255),
  application_type_viewer_url varchar(255),
  defs text,
  description varchar(2048),
  properties text,
  required varchar(255) array,
  schema varchar(255),
  primary key (schema_id)
);

create table application_entity (
  deployment_name varchar(255) not null,
  application_properties varchar(255),
  defaults text,
  description varchar(2048),
  description_keywords varchar(255) array,
  display_name varchar(255),
  display_version varchar(255),
  endpoint varchar(255) check (
    (nullif(endpoint,'') is null and nullif(application_type_schema_id,'') is not null)
    or
    (nullif(endpoint,'') is not null and nullif(application_type_schema_id,'') is null)
  ),
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
  primary key (deployment_name)
);
alter table application_entity add constraint UK_APPLICATION_ENTITY_DISPLAY_NAME_DISPLAY_VERSION unique (display_name, display_version);
alter table application_entity add constraint FK_APPLICATION_TYPE_SCHEMA_ENTITY_APPLICATION_TYPE_SCHEMA_ID foreign key (application_type_schema_id) references application_type_schema_entity (schema_id);
alter table application_entity add constraint FK_APPLICATION_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);

create table key_entity (
  name varchar(255) not null,
  description varchar(2048),
  key_value varchar(255),
  project varchar(255),
  secured boolean not null,
  project_contact_point text,
  created_at_ms bigint not null,
  expires_at_ms bigint,
  key_value_generated_at_ms bigint not null,
  primary key (name)
);
alter table key_entity add constraint UK_KEY_ENTITY_KEY_VALUE unique (key_value);

create table model_entity (
  deployment_name varchar(255) not null,
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
  primary key (deployment_name)
);
alter table model_entity add constraint UK_MODEL_ENTITY_DISPLAY_NAME_DISPLAY_VERSION unique (display_name, display_version);
alter table model_entity add constraint FK_MODEL_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);

create table interceptor_entity (
  name varchar(255) not null,
  description varchar(2048),
  display_name varchar(255),
  endpoint varchar(255),
  forward_auth_token boolean,
  icon_url varchar(255),
  primary key (name)
);

create table interceptor_application (
  interceptor_name varchar(255) not null,
  application_name varchar(255) not null
);
alter table interceptor_application add constraint FK_INTERCEPTOR_APPLICATION_INTERCEPTOR_NAME foreign key (interceptor_name) references interceptor_entity (name);
alter table interceptor_application add constraint FK_INTERCEPTOR_APPLICATION_APPLICATION_NAME foreign key (application_name) references application_entity (deployment_name);

create table interceptor_model (
  interceptor_name varchar(255) not null,
  model_name varchar(255) not null
);
alter table interceptor_model add constraint FK_INTERCEPTOR_MODEL_INTERCEPTOR_NAME foreign key (interceptor_name) references interceptor_entity (name);
alter table interceptor_model add constraint FK_INTERCEPTOR_MODEL_MODEL_NAME foreign key (model_name) references model_entity (deployment_name);

create table role_key (
  role_name varchar(255) not null,
  key_name varchar(255) not null
);
alter table role_key add constraint FK_ROLE_KEY_ROLE_NAME foreign key (role_name) references role_entity (name);
alter table role_key add constraint FK_ROLE_KEY_KEY_NAME foreign key (key_name) references key_entity (name);

create table role_limit_entity (
  deployment_name varchar(255) not null,
  role_name varchar(255) not null,
  enabled boolean not null,
  default_day bigint,
  default_minute bigint,
  default_month bigint,
  default_request_day bigint,
  default_request_hour bigint,
  default_week bigint,
  primary key (deployment_name, role_name)
);
alter table role_limit_entity add constraint FK_ROLE_LIMIT_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);
alter table role_limit_entity add constraint FK_ROLE_LIMIT_ENTITY_ROLE_NAME foreign key (role_name) references role_entity (name);

create table route_entity (
  deployment_name varchar(255) not null,
  description varchar(2048),
  max_retry_attempts integer not null,
  methods varchar(255) array,
  paths varchar(255) array,
  body varchar(255),
  status integer,
  rewrite_path boolean not null,
  upstreams varchar(255),
  primary key (deployment_name)
);
alter table route_entity add constraint FK_ROUTE_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);

create table assistant_entity (
  deployment_name varchar(255) not null,
  defaults text,
  description varchar(2048),
  description_keywords varchar(255) array,
  display_name varchar(255),
  forward_auth_token boolean,
  icon_url varchar(255),
  input_attachment_types varchar(255) array,
  max_input_attachments integer,
  primary key (deployment_name)
);
alter table assistant_entity add constraint FK_ASSISTANT_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);

create table assistants_property_entity (
  id bigint not null,
  endpoint varchar(255),
  rate_endpoint varchar(255),
  tokenize_endpoint varchar(255),
  truncate_prompt_endpoint varchar(255),
  configuration_endpoint varchar(255),
  system_prompt_supported boolean,
  tools_supported boolean,
  seed_supported boolean,
  url_attachments_supported boolean,
  folder_attachments_supported boolean,
  allow_resume boolean,
  accessible_by_per_request_key boolean,
  content_parts_supported boolean,
  temperature_supported boolean,
  addons_supported boolean,
  primary key (id)
);
