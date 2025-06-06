-- MS SQL Server uses "create unique index" syntax in order to create unique constraints on nullable columns (see https://learn.microsoft.com/en-us/sql/relational-databases/tables/create-unique-constraints#TsqlExample)
-- Also MS SQL Server doesn't allow to have "nvarchar(max)" columns participated in index, since maximum nonclustered index size is 1700 bytes.
-- From Microsoft docs (https://learn.microsoft.com/en-us/sql/sql-server/maximum-capacity-specifications-for-sql-server#-objects):
-- "You can define a key using variable-length columns whose maximum sizes add up to more than the limit. However, the combined sizes of the data in those columns can never exceed the limit.".
-- Therefore columns participated in unique indexes:
-- 1. application_entity.display_name
-- 2. application_entity.display_version
-- 3. model_entity.display_name
-- 4. model_entity.display_version
-- 5. key_entity.key_value
-- are left as "nvarchar(255)" in order to not exceed index size limit.
--
-- application_type_schema_entity.schema_id column is nonclustered primary key so that nonclustered index is created implicitly,
-- and therefore the column type is "nvarchar(850)" in order to not exceed index size limit.
--
-- see what n in "nvarchar(n)" type means: https://learn.microsoft.com/en-us/sql/t-sql/data-types/nchar-and-nvarchar-transact-sql#remarks
create table deployment_entity (
  name nvarchar(255) not null,
  default_day bigint,
  default_minute bigint,
  default_month bigint,
  default_request_day bigint,
  default_request_hour bigint,
  default_week bigint,
  is_public bit,
  primary key (name)
);

create table role_entity (
  name nvarchar(255) not null,
  description nvarchar(max),
  primary key (name)
);

create table addon_entity (
  deployment_name nvarchar(255) not null,
  description nvarchar(max),
  display_name nvarchar(max),
  endpoint nvarchar(max),
  forward_auth_token bit,
  icon_url nvarchar(max),
  input_attachment_types varbinary(max),
  max_input_attachments integer,
  primary key (deployment_name)
);
alter table addon_entity add constraint FK_ADDON_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);

create table application_type_schema_entity (
  schema_id nvarchar(850) not null,
  application_type_completion_endpoint nvarchar(max),
  application_type_display_name nvarchar(max),
  application_type_editor_url nvarchar(max),
  application_type_viewer_url nvarchar(max),
  defs nvarchar(max),
  description nvarchar(max),
  properties nvarchar(max),
  required varbinary(max),
  [schema] nvarchar(max),
  primary key nonclustered (schema_id)
);

create table application_entity (
  deployment_name nvarchar(255) not null,
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
  primary key (deployment_name),
  check (
    (nullif(endpoint,'') is null and nullif(application_type_schema_id,'') is not null)
    or
    (nullif(endpoint,'') is not null and nullif(application_type_schema_id,'') is null)
  )
);
create unique nonclustered index UX_APPLICATION_ENTITY_DISPLAY_NAME_DISPLAY_VERSION_NOT_NULL on application_entity (display_name, display_version) where display_name is not null and display_version is not null;
alter table application_entity add constraint FK_APPLICATION_TYPE_SCHEMA_ENTITY_APPLICATION_TYPE_SCHEMA_ID foreign key (application_type_schema_id) references application_type_schema_entity (schema_id);
alter table application_entity add constraint FK_APPLICATION_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);

create table key_entity (
  name nvarchar(255) not null,
  description nvarchar(max),
  key_value nvarchar(255),
  project nvarchar(max),
  secured bit not null,
  project_contact_point nvarchar(max),
  created_at_ms bigint not null,
  expires_at_ms bigint,
  key_value_generated_at_ms bigint not null,
  primary key (name)
);
create unique nonclustered index UX_KEY_ENTITY_KEY_VALUE_NOT_NULL on key_entity (key_value) where key_value is not null;

create table model_entity (
  deployment_name nvarchar(255) not null,
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
  primary key (deployment_name)
);
create unique nonclustered index UX_MODEL_ENTITY_DISPLAY_NAME_DISPLAY_VERSION_NOT_NULL on model_entity (display_name, display_version) where display_name is not null and display_version is not null;
alter table model_entity add constraint FK_MODEL_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);

create table interceptor_entity (
  name nvarchar(255) not null,
  description nvarchar(max),
  display_name nvarchar(max),
  endpoint nvarchar(max),
  forward_auth_token bit,
  icon_url nvarchar(max),
  primary key (name)
);

create table interceptor_application (
  interceptor_name nvarchar(255) not null,
  application_name nvarchar(255) not null
);
alter table interceptor_application add constraint FK_INTERCEPTOR_APPLICATION_INTERCEPTOR_NAME foreign key (interceptor_name) references interceptor_entity (name);
alter table interceptor_application add constraint FK_INTERCEPTOR_APPLICATION_APPLICATION_NAME foreign key (application_name) references application_entity (deployment_name);

create table interceptor_model (
  interceptor_name nvarchar(255) not null,
  model_name nvarchar(255) not null
);
alter table interceptor_model add constraint FK_INTERCEPTOR_MODEL_INTERCEPTOR_NAME foreign key (interceptor_name) references interceptor_entity (name);
alter table interceptor_model add constraint FK_INTERCEPTOR_MODEL_MODEL_NAME foreign key (model_name) references model_entity (deployment_name);

create table role_key (
  role_name nvarchar(255) not null,
  key_name nvarchar(255) not null
);
alter table role_key add constraint FK_ROLE_KEY_ROLE_NAME foreign key (role_name) references role_entity (name);
alter table role_key add constraint FK_ROLE_KEY_KEY_NAME foreign key (key_name) references key_entity (name);

create table role_limit_entity (
  deployment_name nvarchar(255) not null,
  role_name nvarchar(255) not null,
  enabled bit not null,
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
  deployment_name nvarchar(255) not null,
  description nvarchar(max),
  max_retry_attempts integer not null,
  methods varbinary(255),
  paths varbinary(max),
  body nvarchar(max),
  status integer,
  rewrite_path bit not null,
  upstreams nvarchar(max),
  primary key (deployment_name)
);
alter table route_entity add constraint FK_ROUTE_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);

create table assistant_entity (
  deployment_name nvarchar(255) not null,
  defaults nvarchar(max),
  description nvarchar(max),
  description_keywords varbinary(max),
  display_name nvarchar(max),
  forward_auth_token bit,
  icon_url nvarchar(max),
  input_attachment_types varbinary(max),
  max_input_attachments integer,
  primary key (deployment_name)
);
alter table assistant_entity add constraint FK_ASSISTANT_ENTITY_DEPLOYMENT_NAME foreign key (deployment_name) references deployment_entity (name);

create table assistants_property_entity (
  id bigint not null,
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
  primary key (id)
);
