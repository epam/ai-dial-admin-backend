create table interceptor_runner_entity (
  name varchar(255) not null,
  display_name text,
  description text,
  completion_endpoint text,
  configuration_endpoint text,
  primary key (name)
);

alter table interceptor_entity add configuration_endpoint text;
alter table interceptor_entity add interceptor_runner_name varchar(255);

alter table interceptor_entity add constraint FK_INTERCEPTOR_ENTITY_INTERCEPTOR_RUNNER_NAME
    foreign key (interceptor_runner_name) references interceptor_runner_entity (name);