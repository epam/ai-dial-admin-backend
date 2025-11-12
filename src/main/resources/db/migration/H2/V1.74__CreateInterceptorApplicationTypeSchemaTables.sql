-- Create interceptor_application_type_schema table
create table interceptor_application_type_schema (
  interceptor_name varchar(255) not null,
  application_type_schema_id varchar(255) not null,
  interceptors_order integer not null,
  primary key (
      interceptor_name, application_type_schema_id, interceptors_order
    )
);

alter table interceptor_application_type_schema add constraint FK_INTERCEPTOR_APPLICATION_TYPE_SCHEMA_INTERCEPTOR_NAME foreign key (interceptor_name) references interceptor_entity (name);
alter table interceptor_application_type_schema add constraint FK_INTERCEPTOR_APPLICATION_TYPE_SCHEMA_APPLICATION_TYPE_SCHEMA_ID foreign key (application_type_schema_id) references application_type_schema_entity (schema_id);

-- Create interceptor_application_type_schema_aud table
create table interceptor_application_type_schema_aud (
  rev integer not null,
  interceptor_name varchar(255) not null,
  application_type_schema_id varchar(255) not null,
  interceptors_order integer not null,
  revtype smallint,
  primary key (
    interceptor_name, rev, application_type_schema_id, interceptors_order
  )
);