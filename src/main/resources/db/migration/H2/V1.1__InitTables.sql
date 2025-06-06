create table deployment_entity (
  id bigint not null,
  default_day bigint,
  default_minute bigint,
  default_month bigint,
  default_request_day bigint,
  default_request_hour bigint,
  default_week bigint,
  is_public boolean,
  name varchar(255),
  primary key (id)
);
alter table deployment_entity add constraint UK5m4rgdmp7mlewc6wdapg00d8g unique (name);
create sequence if not exists deployment_entity_seq start with 1 increment by 50;

create table role_entity (
  id bigint not null,
  description varchar(255),
  name varchar(255),
  primary key (id)
);
alter table role_entity add constraint UK2UQXLFG1DLWV0MTEWROKR23OU unique (name);
create sequence if not exists role_entity_seq start with 1 increment by 50;

create table addon_entity (
  id bigint not null,
  description varchar(255),
  display_name varchar(255),
  endpoint varchar(255),
  forward_auth_token boolean,
  icon_url varchar(255),
  input_attachment_types varchar(255) array,
  max_input_attachments integer,
  deployment_id bigint,
  primary key (id)
);
alter table addon_entity add constraint UK7jv2fxv5xwg2h12wwy4dvcu3h unique (deployment_id);
alter table addon_entity add constraint FKgiok8p6klqlvpgk63gmd36492 foreign key (deployment_id) references deployment_entity (id);
create sequence if not exists addon_entity_seq start with 1 increment by 50;

create table application_type_schema_entity (
  id bigint not null,
  application_type_completion_endpoint varchar(255),
  application_type_display_name varchar(255),
  application_type_editor_url varchar(255),
  application_type_viewer_url varchar(255),
  defs clob,
  description varchar(255),
  properties varchar(255),
  required varchar(255) array,
  schema varchar(255),
  schema_id varchar(255),
  primary key (id)
);
alter table application_type_schema_entity add constraint UK3r0c7hhvq8t10xbco2m0f82lh unique (schema_id);
create sequence if not exists application_type_schema_entity_seq start with 1 increment by 50;

create table application_entity (
  id bigint not null,
  application_properties varchar(255),
  defaults varchar(255),
  description varchar(255),
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
  deployment_id bigint,
  primary key (id)
);
alter table application_entity add constraint UKfedqns18ddue6lqnknori2lkg unique (deployment_id);
alter table application_entity add constraint FKbk70tyy2op3ls9cq6kfrtgl36 foreign key (application_type_schema_id) references application_type_schema_entity (schema_id);
alter table application_entity add constraint FKl99rl4fprycgy7r3dn6n7flp foreign key (deployment_id) references deployment_entity (id);
create sequence if not exists application_entity_seq start with 1 increment by 50;

create table key_entity (
  id bigint not null,
  description varchar(255),
  key_name varchar(255),
  project varchar(255),
  secured boolean not null,
  primary key (id)
);
alter table key_entity add constraint UKiwn1lk6lcml3aihefbb48v5mw unique (key_name);
create sequence if not exists key_entity_seq start with 1 increment by 50;

create table model_entity (
  id bigint not null,
  defaults varchar(255),
  description varchar(255),
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
  type tinyint check (
    type between 0
    and 2
  ),
  upstreams varchar(255),
  deployment_id bigint,
  primary key (id)
);
alter table model_entity add constraint UK10h4xph5w4hlo6amphewevu0h unique (deployment_id);
alter table model_entity add constraint FK4ria7nx9plw33ifw7ht6p20me foreign key (deployment_id) references deployment_entity (id);
create sequence if not exists model_entity_seq start with 1 increment by 50;

create table interceptor_entity (
  id bigint not null,
  description varchar(255),
  display_name varchar(255),
  endpoint varchar(255),
  forward_auth_token boolean,
  icon_url varchar(255),
  name varchar(255),
  primary key (id)
);
alter table interceptor_entity add constraint UK8u9u7k211s99tjq66iaamhqdw unique (name);
create sequence if not exists interceptor_entity_seq start with 1 increment by 50;

create table interceptor_application (
  interceptor_id bigint not null,
  application_id bigint not null
);
alter table interceptor_application add constraint FKix5nobawvhjm3f1c8o7kp5veg foreign key (application_id) references application_entity (id);
alter table interceptor_application add constraint FKdv9qpbyhyy80s8gv5l36qllew foreign key (interceptor_id) references interceptor_entity (id);

create table interceptor_model (
  interceptor_id bigint not null,
  model_id bigint not null
);
alter table interceptor_model add constraint FK5b3mth7nfcm2c43cg6rgblhgg foreign key (model_id) references model_entity (id);
alter table interceptor_model add constraint FK5dimtnlja7ph2irlwry0wfse foreign key (interceptor_id) references interceptor_entity (id);

create table role_key (
  key_id bigint not null,
  role_id bigint not null
);
alter table role_key add constraint FKogyyrc4cxdlxet96qetiffnie foreign key (role_id) references role_entity (id);
alter table role_key add constraint FKrk8uq8nd447iomos5mxsckrmv foreign key (key_id) references key_entity (id);

create table role_limit_entity (
  id bigint not null,
  deployment_id bigint not null,
  enabled boolean not null,
  default_day bigint,
  default_minute bigint,
  default_month bigint,
  default_request_day bigint,
  default_request_hour bigint,
  default_week bigint,
  role_id bigint not null,
  primary key (id)
);
alter table role_limit_entity add constraint FKm1gmk76tjmoexcufwjnkpbxnn foreign key (role_id) references role_entity (id);
alter table role_limit_entity add constraint FKqx3e4spegvt9oslr7580p1b7b foreign key (deployment_id) references deployment_entity (id);
create sequence if not exists role_limit_entity_seq start with 1 increment by 50;

create table route_entity (
  id bigint not null,
  description varchar(255),
  max_retry_attempts integer not null,
  methods varchar(255) array,
  paths varchar(255) array,
  body varchar(255),
  status integer,
  rewrite_path boolean not null,
  upstreams varchar(255),
  name_id bigint,
  deployment_id bigint,
  primary key (id)
);
alter table route_entity add constraint UKLNXKGPIQT5VSG5A59OWOUO6NF unique (name_id);
alter table route_entity add constraint UKS6AK472C4345RSJ8UQMFMSS9F unique (deployment_id);
alter table route_entity add constraint FKFT47F8SQO9NI1Y08IL5QGBW6Q foreign key (name_id) references deployment_entity (id);
alter table route_entity add constraint FKANPI2YK95373XTRTN9GK22AVV foreign key (deployment_id) references deployment_entity (id);
create sequence if not exists route_entity_seq start with 1 increment by 50;

create table upstream_entity (
	id bigint not null,
	endpoint varchar(255),
	extra_data varchar(255),
	key_name varchar(255),
	tier integer not null,
	weight integer not null,
	primary key (id)
);
create sequence if not exists upstream_entity_seq start with 1 increment by 50;
